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
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
public class AnalysisService {

    private final UserRepository userRepository;
    private final UrlRepository urlRepository;
    private final UrlAnalysisRepository urlAnalysisRepository;
    private final AnalysisHistoryRepository analysisHistoryRepository;
    private final NotificationRepository notificationRepository;
    private final DomainReputationRepository domainReputationRepository; // 평판 조회용 레포지토리 추가

    // LLM 팀과 협의 전 사용할 1차 규칙 버전
    private static final String CURRENT_RULE_VERSION = "ruleset-1.1.0";

    // 규칙 1: 도메인 대신 IP 주소(예: 192.168.0.1)를 직접 사용하는지 검사하는 정규식
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
    );

    // 규칙 2: 피싱에 자주 악용되는 의심스러운 TLD (수정 및 확장 가능)
    private static final List<String> SUSPICIOUS_TLDS = Arrays.asList(
            ".top", ".xyz", ".club", ".biz", ".info", ".tk", ".ml", ".ga", ".cf", ".gq"
    );

    /**
     * URL 분석 실행 및 저장 (규칙 기반 엔진 가동)
     */
    public UrlAnalysis analyzeAndSave(Long userId, Long urlId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new IllegalArgumentException("URL을 찾을 수 없습니다."));

        String rawUrl = url.getNormalizedUrl();
        String extractedDomain = extractDomain(rawUrl);

        // ==============================================================
        // 🚨 규칙 기반 분석 엔진 가동 (Risk Score 계산)
        // ==============================================================
        int riskScore = 0;
        List<String> reasonList = new ArrayList<>();

        // 1. 도메인 평판 조회 (DB)
        DomainReputation reputation = domainReputationRepository.findByDomain(extractedDomain).orElse(null);

        // [최우선 규칙] 블랙리스트/화이트리스트 즉시 판별
        if (reputation != null && Boolean.TRUE.equals(reputation.getIsBlacklisted())) {
            riskScore = 100;
            reasonList.add("블랙리스트에 등록된 악성 도메인");
        } else if (reputation != null && Boolean.TRUE.equals(reputation.getIsWhitelisted())) {
            riskScore = 0;
            reasonList.add("안전이 검증된 화이트리스트 도메인");
        } else {
            // [규칙 A] IP 기반 URL 검사 (+40점)
            if (IP_PATTERN.matcher(extractedDomain).matches()) {
                riskScore += 40;
                reasonList.add("도메인 대신 IP 주소 직접 사용");
            }

            // [규칙 B] URL 길이 검사 (난독화 목적으로 75자 초과 시 +20점)
            if (rawUrl.length() > 75) {
                riskScore += 20;
                reasonList.add("비정상적으로 긴 URL 구조 (" + rawUrl.length() + "자)");
            }

            // [규칙 C] 의심스러운 TLD 검사 (+30점)
            boolean hasSuspiciousTld = SUSPICIOUS_TLDS.stream().anyMatch(extractedDomain::endsWith);
            if (hasSuspiciousTld) {
                riskScore += 30;
                reasonList.add("피싱 악용 빈도가 높은 최상위 도메인(TLD) 포함");
            }

            // [규칙 D] 도메인 신뢰도 점수(Trust Score) 반영 (40점 미만 시 +20점)
            if (reputation != null && reputation.getTrustScoreValue() < 40.0) {
                riskScore += 20;
                reasonList.add("도메인 평판(신뢰도) 점수 낮음");
            }
        }

        // 점수 보정 (최대 100점을 넘지 않도록)
        riskScore = Math.min(riskScore, 100);

        // ==============================================================
        // 📊 최종 위험도(RiskLevel) 등급 판별
        // ==============================================================
        RiskLevel riskLevel;
        if (riskScore >= 80) riskLevel = RiskLevel.CRITICAL;
        else if (riskScore >= 60) riskLevel = RiskLevel.DANGER;
        else if (riskScore >= 30) riskLevel = RiskLevel.WARNING;
        else riskLevel = RiskLevel.SAFE;

        if (reasonList.isEmpty()) {
            reasonList.add("특이사항 없음 (안전)");
        }
        String finalReasonSummary = String.join(" / ", reasonList);

        // ==============================================================
        // 💾 분석 결과 영속화 (DB 저장)
        // ==============================================================
        UrlAnalysis analysis = UrlAnalysis.builder()
                .url(url)
                .domain(extractedDomain)
                .sourceType("MAIL") 
                .riskLevel(riskLevel)
                .riskType(riskLevel == RiskLevel.SAFE ? "NONE" : "PHISHING") 
                .score(BigDecimal.valueOf(riskScore))
                .reasonSummary(finalReasonSummary)
                .ruleVersion(CURRENT_RULE_VERSION) 
                .sslVerified(rawUrl.startsWith("https"))
                .redirectionDepth(0)
                .containsFormInput(false)
                .featuresJson("{}")
                .analyzedAt(LocalDateTime.now())
                .build();

        UrlAnalysis savedAnalysis = urlAnalysisRepository.save(analysis);
        
        // 히스토리 기록
        analysisHistoryRepository.save(AnalysisHistory.createHistory(user, savedAnalysis, "MAIL"));

        // DANGER 이상일 경우에만 알림 생성 및 디스코드 전송
        if (riskLevel == RiskLevel.DANGER || riskLevel == RiskLevel.CRITICAL) {
            createRiskNotification(user, savedAnalysis); 
            sendDiscordAlert(savedAnalysis); 
        }
        
        return savedAnalysis;
    }

    @Transactional(readOnly = true)
    public AnalysisDetailResponseDto getDetail(Long analysisId) {
        UrlAnalysis analysis = urlAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("분석 결과가 없습니다."));
        return AnalysisDetailResponseDto.from(analysis);
    }

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

    private void createRiskNotification(User user, UrlAnalysis analysis) {
        Notification noti = Notification.builder()
                .user(user)
                .analysisId(analysis.getAnalysisId())
                .channel("WEB")
                .title("🚨 메일 내 위험 URL 감지")
                .message("수신된 메일에서 [" + analysis.getRiskLevel().name() + "] 등급의 위험 피싱 URL이 발견되었습니다.")
                .isRead(false)
                .readAt(null)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(noti);
    }

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
                "🚨 **위험 URL 탐지 알림**\n🔗 URL: %s\n등급: [%s] (점수: %s점)\n사유: %s",
                analysis.getUrl().getNormalizedUrl(),
                levelKor,
                analysis.getScore(),
                analysis.getReasonSummary()
            );
            
            body.put("content", content);
            restTemplate.postForEntity(webhookUrl, body, String.class);
        } catch (Exception e) {
            System.err.println("디스코드 전송 실패: " + e.getMessage());
        }
    }
}