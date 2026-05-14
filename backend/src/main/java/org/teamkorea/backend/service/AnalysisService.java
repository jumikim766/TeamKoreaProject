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

    public UrlAnalysis analyzeAndSave(Long userId, Long urlId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new IllegalArgumentException("URL을 찾을 수 없습니다."));

        RiskLevel riskLevel = RiskLevel.DANGER; 
        String extractedDomain = extractDomain(url.getNormalizedUrl()); 

        UrlAnalysis analysis = UrlAnalysis.builder()
                .url(url)
                .domain(extractedDomain)
                .sourceType("MAIL") 
                .riskLevel(riskLevel)
                .riskType("PHISHING") 
                .score(BigDecimal.valueOf(85.0))
                .reasonSummary("피싱 의심 도메인 및 비정상적 구조 탐지")
                .ruleVersion(CURRENT_RULE_VERSION)
                .sslVerified(true)
                .redirectionDepth(0)
                .containsFormInput(false)
                .featuresJson("{}")
                .analyzedAt(LocalDateTime.now())
                .build();

        UrlAnalysis savedAnalysis = urlAnalysisRepository.save(analysis);
        
        AnalysisHistory history = AnalysisHistory.builder()
                .user(user)
                .urlAnalysis(savedAnalysis) 
                .source("MAIL") 
                .build();
        analysisHistoryRepository.save(history);

        if (riskLevel != RiskLevel.SAFE) {
            createRiskNotification(user, savedAnalysis); 
            sendDiscordAlert(savedAnalysis); 
        }
        
        return savedAnalysis;
    }

    @Transactional(readOnly = true)
    public AnalysisDetailResponseDto getDetail(Long analysisId) {
        UrlAnalysis analysis = urlAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("분석 결과가 없습니다."));
        return new AnalysisDetailResponseDto(analysis);
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
                .urlAnalysis(analysis)
                .channel("WEB") 
                .title("🚨 위험 URL 감지")
                .message("[" + analysis.getRiskLevel().name() + "] 등급의 URL 발견")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(noti);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationResponseDto::from)
                .collect(Collectors.toList());
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
            Map<String, Object> body = new HashMap<>();
            body.put("content", "🚨 위험 URL 탐지 알림: " + analysis.getUrl().getNormalizedUrl());
            restTemplate.postForEntity(webhookUrl, body, String.class);
        } catch (Exception e) {
            System.err.println("디스코드 전송 실패");
        }
    }
}