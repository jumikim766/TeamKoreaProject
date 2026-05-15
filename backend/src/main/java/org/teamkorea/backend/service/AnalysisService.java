package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.teamkorea.backend.domain.*;
import org.teamkorea.backend.dto.*;
import org.teamkorea.backend.repository.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AnalysisService {

    private final UserRepository userRepository;
    private final UrlRepository urlRepository;
    private final UrlAnalysisRepository urlAnalysisRepository;
    private final AnalysisHistoryRepository analysisHistoryRepository;
    private final NotificationRepository notificationRepository;

    private static final String CURRENT_RULE_VERSION = "ruleset-1.0.0";

    /**
     * URL 분석 실행 및 저장
     */
    public UrlAnalysis analyzeAndSave(Long userId, Long urlId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new IllegalArgumentException("URL을 찾을 수 없습니다."));

        RiskLevel riskLevel = RiskLevel.DANGER; 
        String extractedDomain = extractDomain(url.getNormalizedUrl()); 

        // 1. UrlAnalysis 엔티티 빌드 (ruleVersion 및 필수 필드 포함)
        UrlAnalysis analysis = UrlAnalysis.builder()
                .url(url)
                .domain(extractedDomain)
                .sourceType("MAIL") 
                .riskLevel(riskLevel)
                .riskType("PHISHING") 
                .score(BigDecimal.valueOf(85.0))
                .reasonSummary("피싱 의심 도메인 및 비정상적 구조 탐지")
                .ruleVersion(CURRENT_RULE_VERSION) // [해결] rule_version NOT NULL 충족
                .sslVerified(true)
                .redirectionDepth(0)
                .containsFormInput(false)
                .featuresJson("{}")
                .analyzedAt(LocalDateTime.now())
                .build();

        UrlAnalysis savedAnalysis = urlAnalysisRepository.save(analysis);
        
        // 2. AnalysisHistory 저장 (static 메서드 활용)
        analysisHistoryRepository.save(AnalysisHistory.createHistory(user, savedAnalysis, "MAIL"));

        // 3. 알림 및 디스코드 전송
        if (riskLevel != RiskLevel.SAFE) {
            createRiskNotification(user, savedAnalysis); 
            sendDiscordAlert(savedAnalysis); 
        }
        
        return savedAnalysis;
    }

    /**
     * 상세 분석 결과 조회 (기존 DTO 생성자 사용)
     */
    @Transactional(readOnly = true)
    public AnalysisDetailResponseDto getDetail(Long analysisId) {
        UrlAnalysis analysis = urlAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("분석 결과가 없습니다."));
        return AnalysisDetailResponseDto.from(analysis);
    }

    /**
     * 내 분석 히스토리 페이징 조회
     */
    @Transactional(readOnly = true)
    public Page<AnalysisHistoryResponseDto> getAnalysisList(User user, Pageable pageable) {
        return analysisHistoryRepository.findByUser(user, pageable)
                .map(history -> {
                    UrlAnalysis ua = history.getUrlAnalysis();
                    return AnalysisHistoryResponseDto.builder()
                            .historyId(history.getHistoryId())
                            .analysisId(ua.getAnalysisId())
                            .url(ua.getUrl().getNormalizedUrl())
                            .riskLevel(ua.getRiskLevel().name())
                            .source(history.getSource())
                            .createdAt(history.getCreatedAt().toString())
                            .build();
                });
    }

    /**
     * 내부 시스템 알림 생성
     */
    private void createRiskNotification(User user, UrlAnalysis analysis) {
        Notification noti = Notification.builder()
                .user(user)
                .urlAnalysis(analysis)
                .channel("MAIL")
                .title("🚨 메일 내 위험 URL 감지")
                .message("수신된 메일에서 [" + analysis.getRiskLevel().name() + "] 등급의 URL이 발견되었습니다.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(noti);
    }

    /**
     * 내 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 도메인 추출 유틸리티
     */
    private String extractDomain(String urlString) {
        try {
            URI uri = new URI(urlString);
            String host = uri.getHost();
            if (host == null) return "unknown";
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (Exception e) {
            return "unknown"; 
        }
    }

    /**
     * 디스코드 알림 발송 (한국어 등급 및 이모지 적용)
     */
    private void sendDiscordAlert(UrlAnalysis analysis) {
        try {
            String webhookUrl = "YOUR_DISCORD_WEBHOOK_URL"; 
            RestTemplate restTemplate = new RestTemplate();

            String levelKor = switch (analysis.getRiskLevel()) {
                case SAFE -> "안전";
                case WARNING -> "주의";
                case DANGER -> "위험";
                case CRITICAL -> "심각";
                default -> "알 수 없음";
            };

            Map<String, Object> body = new HashMap<>();
            String content = String.format(
                "🚨 **위험 URL 탐지 알림**\n🔗 URL: %s\n등급: [%s]\n사유: %s",
                analysis.getUrl().getNormalizedUrl(),
                levelKor,
                analysis.getReasonSummary()
            );
            
            body.put("content", content);
            restTemplate.postForEntity(webhookUrl, body, String.class);
        } catch (Exception e) {
            System.err.println("디스코드 전송 실패: " + e.getMessage());
        }
    }
}