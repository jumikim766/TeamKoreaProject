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
                .reasonSummary("피싱 의심 도메인, 비정상적 URL 길이")
                .analyzedAt(LocalDateTime.now())
                .build();

        UrlAnalysis saved = urlAnalysisRepository.save(analysis);
        analysisHistoryRepository.save(AnalysisHistory.createHistory(user, saved, "WEB"));

        if (riskLevel == RiskLevel.DANGER || riskLevel == RiskLevel.CRITICAL) {
            createRiskNotification(user, saved);
            sendDiscordAlert(saved); // [추가] 디스코드 알림 발송
        }
        return saved;
    }

    private void createRiskNotification(User user, UrlAnalysis analysis) {
        Notification noti = Notification.builder()
                .user(user)
                .urlAnalysis(analysis)
                .channel("WEB")
                .title("🚨 위험 URL 탐지 알림")
                .message("[" + analysis.getUrl().getNormalizedUrl() + "] 위험 등급 위협이 감지되었습니다.")
                .isRead(false)
                .build();
        notificationRepository.save(noti);
    }

    // [추가] 디스코드 웹훅 연동 로직
    private void sendDiscordAlert(UrlAnalysis analysis) {
        try {
            String webhookUrl = "YOUR_DISCORD_WEBHOOK_URL"; // 실제 웹훅 주소로 변경 필요
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> body = new HashMap<>();
            body.put("content", "⚠️ **[위험 URL 감지]**\n- URL: " + analysis.getUrl().getNormalizedUrl() + "\n- 등급: " + analysis.getRiskLevel());
            restTemplate.postForEntity(webhookUrl, body, String.class);
        } catch (Exception e) {
            System.out.println("디스코드 알림 전송 실패: " + e.getMessage());
        }
    }

    // [추가] 알림 읽음 처리 기능
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification noti = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));
        noti.setIsRead(true);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationResponseDto::from)
                .collect(Collectors.toList());
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
                .map(history -> AnalysisHistoryResponseDto.builder()
                        .historyId(history.getHistoryId())
                        .analysisId(history.getUrlAnalysis().getAnalysisId())
                        .url(history.getUrlAnalysis().getUrl().getNormalizedUrl())
                        .riskLevel(history.getUrlAnalysis().getRiskLevel().name())
                        .createdAt(history.getCreatedAt().toString())
                        .build());
    }

    // [추가] 대시보드용 통계 데이터 조회
    @Transactional(readOnly = true)
    public Map<String, Long> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", urlAnalysisRepository.count());
        stats.put("danger", urlAnalysisRepository.countByRiskLevel(RiskLevel.DANGER));
        stats.put("safe", urlAnalysisRepository.countByRiskLevel(RiskLevel.SAFE));
        return stats;
    }
}