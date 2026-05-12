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

    /**
     * URL 분석 실행 및 저장
     */
    public UrlAnalysis analyzeAndSave(Long userId, Long urlId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new IllegalArgumentException("URL을 찾을 수 없습니다."));

        RiskLevel riskLevel = RiskLevel.DANGER; 

        UrlAnalysis analysis = UrlAnalysis.builder()
                .url(url)
                .riskLevel(riskLevel)
                .score(BigDecimal.valueOf(85.0))
                .reasonSummary("피싱 의심 도메인, 비정상적 URL 구조")
                .analyzedAt(LocalDateTime.now())
                .build();

        UrlAnalysis saved = urlAnalysisRepository.save(analysis);
        analysisHistoryRepository.save(AnalysisHistory.createHistory(user, saved, "MAIL"));

        if (riskLevel != RiskLevel.SAFE) {
            createRiskNotification(user, saved);
            sendDiscordAlert(saved); 
        }
        return saved;
    }

    /**
     * 디스코드 알림 발송 (한국어 등급 표기)
     */
    private void sendDiscordAlert(UrlAnalysis analysis) {
        try {
            String webhookUrl = "YOUR_DISCORD_WEBHOOK_URL"; 
            RestTemplate restTemplate = new RestTemplate();

            String levelKor;
            String emoji;
            switch (analysis.getRiskLevel()) {
                case SAFE -> { levelKor = "안전"; emoji = "✅"; }
                case WARNING -> { levelKor = "주의"; emoji = "⚠️"; }
                case DANGER -> { levelKor = "위험"; emoji = "🚨"; }
                case CRITICAL -> { levelKor = "심각"; emoji = "💀"; }
                default -> { levelKor = "알 수 없음"; emoji = "❓"; }
            }

            Map<String, Object> body = new HashMap<>();
            String content = String.format(
                "📩 **새로운 메일이 도착했습니다!**\n" +
                "본문에 포함된 URL 분석 결과입니다.\n\n" +
                "🔗 **분석 URL:** %s\n" +
                "%s **위험 등급:** [%s]\n" +
                "📝 **탐지 사유:** %s\n\n" +
                "※ 의심스러운 링크는 절대 클릭하지 마세요!",
                analysis.getUrl().getNormalizedUrl(),
                emoji,
                levelKor,
                analysis.getReasonSummary()
            );
            
            body.put("content", content);
            restTemplate.postForEntity(webhookUrl, body, String.class);
            
        } catch (Exception e) {
            System.err.println("디스코드 전송 실패: " + e.getMessage());
        }
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
     * 상세 분석 결과 조회
     */
    @Transactional(readOnly = true)
    public AnalysisDetailResponseDto getDetail(Long analysisId) {
        UrlAnalysis analysis = urlAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("분석 결과가 없습니다."));
        return AnalysisDetailResponseDto.from(analysis);
    }

    /**
     * [수정 완료] 내 분석 히스토리 페이징 조회
     * Object 타입을 Page<AnalysisHistoryResponseDto>로 변경하고 로직을 구현했습니다.
     */
    @Transactional(readOnly = true)
    public Page<AnalysisHistoryResponseDto> getAnalysisList(User user, Pageable pageable) {
        return analysisHistoryRepository.findByUser(user, pageable)
                .map(history -> AnalysisHistoryResponseDto.builder()
                        .historyId(history.getHistoryId())
                        .analysisId(history.getUrlAnalysis().getAnalysisId())
                        .url(history.getUrlAnalysis().getUrl().getNormalizedUrl())
                        .riskLevel(history.getUrlAnalysis().getRiskLevel().name())
                        .source(history.getSource())
                        .createdAt(history.getCreatedAt().toString())
                        .build());
    }
}