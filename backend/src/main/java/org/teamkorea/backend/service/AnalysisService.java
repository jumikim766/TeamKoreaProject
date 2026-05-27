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
import org.teamkorea.backend.ai.LlmAnalysisService;
import org.teamkorea.backend.ai.dto.LlmAnalysisResponse;

@Service
@Transactional
@RequiredArgsConstructor
public class AnalysisService {

    private final UserRepository userRepository;
    private final UrlRepository urlRepository;
    private final UrlAnalysisRepository urlAnalysisRepository;
    private final AnalysisHistoryRepository analysisHistoryRepository;
    private final NotificationRepository notificationRepository;
    private final DomainReputationRepository domainReputationRepository;
    private final LlmAnalysisService llmAnalysisService;

    private static final String CURRENT_RULE_VERSION = "ruleset-1.2.0"; // 점수 보정 버전 업

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
    );

    private static final List<String> SUSPICIOUS_TLDS = Arrays.asList(
            ".top", ".xyz", ".club", ".biz", ".info", ".tk", ".ml", ".ga", ".cf", ".gq"
    );

    /**
     * URL 분석 실행 및 저장 (점수 보정 메커니즘 적용)
     */
    public UrlAnalysis analyzeAndSave(Long userId, Long urlId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new IllegalArgumentException("URL을 찾을 수 없습니다."));

        String rawUrl = url.getNormalizedUrl();
        String extractedDomain = extractDomain(rawUrl);

        int riskScore = 0;
        List<String> reasonList = new ArrayList<>();

        // 1. 도메인 평판 데이터 조회
        DomainReputation reputation = domainReputationRepository.findByDomain(extractedDomain).orElse(null);

        // [우선순위 1] 블랙리스트 / 화이트리스트 필터링
        if (reputation != null && Boolean.TRUE.equals(reputation.getIsBlacklisted())) {
            riskScore = 100;
            reasonList.add("블랙리스트에 등록된 악성 도메인");
        } else if (reputation != null && Boolean.TRUE.equals(reputation.getIsWhitelisted())) {
            riskScore = 0;
            reasonList.add("안전이 검증된 화이트리스트 도메인");
        } else {
            // [보정 규칙 A] IP 기반 URL 검사 (베이스 50점 부여로 위험도 상향 조정)
            if (IP_PATTERN.matcher(extractedDomain).matches()) {
                riskScore += 50;
                reasonList.add("도메인 대신 IP 주소 직접 사용 (피싱 징후 유력)");
            }

            // [보정 규칙 B] URL 길이 검사 (난독화 패턴 탐지)
            if (rawUrl.length() > 75) {
                riskScore += 20;
                reasonList.add("비정상적으로 긴 URL 구조 (" + rawUrl.length() + "자)");
            }

            // [보정 규칙 C] 의심스러운 저가형 TLD 검사
            boolean hasSuspiciousTld = SUSPICIOUS_TLDS.stream().anyMatch(extractedDomain::endsWith);
            if (hasSuspiciousTld) {
                riskScore += 25;
                reasonList.add("피싱 악용 빈도가 높은 최상위 도메인(TLD) 사용");
            }

            // [보정 규칙 D] 도메인 신뢰 스코어 반영
            if (reputation != null && reputation.getTrustScoreValue() < 40.0) {
                riskScore += 15;
                reasonList.add("도메인 평판 신뢰 점수 미달");
            }
        }

        // 점수 보정 최댓값 제한
        riskScore = Math.min(riskScore, 100);

        // ==============================================================
        // 📊 보정된 등급 판별 스펙 규칙
        // ==============================================================
        RiskLevel riskLevel;
        if (riskScore >= 80) riskLevel = RiskLevel.CRITICAL;
        else if (riskScore >= 55) riskLevel = RiskLevel.DANGER;
        else if (riskScore >= 30) riskLevel = RiskLevel.WARNING;
        else riskLevel = RiskLevel.SAFE;

        if (reasonList.isEmpty()) {
            reasonList.add("특이사항 없음 (안전)");
        }
        String finalReasonSummary = String.join(" / ", reasonList);

        // 2. UrlAnalysis 엔티티 빌드 및 저장
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
        
        // 히스토리 저장
        analysisHistoryRepository.save(AnalysisHistory.createHistory(user, savedAnalysis, "MAIL"));

        // DANGER(55점) 이상일 경우 실시간 알림 트리거 활성화
        if (riskLevel == RiskLevel.DANGER || riskLevel == RiskLevel.CRITICAL) {
            createRiskNotification(user, savedAnalysis); 
            sendDiscordAlert(savedAnalysis); 
        }
        
        return savedAnalysis;
    }
    public UrlAnalysis analyzeWithLlmAndSave(
        Long userId,
        Long urlId,
        String emailSubject,
        String emailBody
) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    Url url = urlRepository.findById(urlId)
            .orElseThrow(() -> new IllegalArgumentException("URL을 찾을 수 없습니다."));

    String rawUrl = url.getNormalizedUrl();
    String extractedDomain = extractDomain(rawUrl);

    int ruleScore = 0;
    List<String> ruleReasons = new ArrayList<>();

    DomainReputation reputation =
            domainReputationRepository.findByDomain(extractedDomain).orElse(null);

    if (reputation != null && Boolean.TRUE.equals(reputation.getIsBlacklisted())) {
        ruleScore = 100;
        ruleReasons.add("블랙리스트에 등록된 악성 도메인");
    } else if (reputation != null && Boolean.TRUE.equals(reputation.getIsWhitelisted())) {
        ruleScore = 0;
        ruleReasons.add("안전이 검증된 화이트리스트 도메인");
    } else {
        if (IP_PATTERN.matcher(extractedDomain).matches()) {
            ruleScore += 50;
            ruleReasons.add("도메인 대신 IP 주소 직접 사용");
        }

        if (!rawUrl.startsWith("https://")) {
            ruleScore += 20;
            ruleReasons.add("HTTPS 미사용");
        }

        if (rawUrl.length() > 75) {
            ruleScore += 20;
            ruleReasons.add("URL 길이 과다");
        }

        if (extractedDomain.chars().filter(ch -> ch == '-').count() >= 2) {
            ruleScore += 15;
            ruleReasons.add("하이픈 과다 사용");
        }

        if (extractedDomain.split("\\.").length >= 4) {
            ruleScore += 15;
            ruleReasons.add("서브도메인 과다 사용");
        }

        boolean hasSuspiciousTld =
                SUSPICIOUS_TLDS.stream().anyMatch(extractedDomain::endsWith);

        if (hasSuspiciousTld) {
            ruleScore += 25;
            ruleReasons.add("의심 TLD 사용");
        }

        String lowerUrl = rawUrl.toLowerCase();

        if (lowerUrl.contains("login")) {
            ruleScore += 20;
            ruleReasons.add("login 키워드 포함");
        }

        if (lowerUrl.contains("verify")) {
            ruleScore += 15;
            ruleReasons.add("verify 키워드 포함");
        }

        if (lowerUrl.contains("password")) {
            ruleScore += 20;
            ruleReasons.add("password 키워드 포함");
        }

        if (lowerUrl.contains("account")) {
            ruleScore += 15;
            ruleReasons.add("account 키워드 포함");
        }

        if (lowerUrl.contains("secure")) {
            ruleScore += 15;
            ruleReasons.add("secure 키워드 포함");
        }

        if (
                lowerUrl.contains("bank")
                        || lowerUrl.contains("pay")
                        || lowerUrl.contains("billing")
                        || lowerUrl.contains("confirm")
                        || lowerUrl.contains("update")
        ) {
            ruleScore += 20;
            ruleReasons.add("금융/결제/인증 관련 키워드 포함");
        }

        if (
                lowerUrl.contains("bit.ly")
                        || lowerUrl.contains("tinyurl.com")
                        || lowerUrl.contains("t.co")
                        || lowerUrl.contains("url.kr")
        ) {
            ruleScore += 30;
            ruleReasons.add("단축 URL 사용");
        }

        if (reputation != null && reputation.getTrustScoreValue() < 40.0) {
            ruleScore += 15;
            ruleReasons.add("도메인 평판 신뢰 점수 미달");
        }
    }

    ruleScore = Math.min(ruleScore, 100);

    String ruleRisk = determineRiskByFinalScore(ruleScore);

   LlmAnalysisResponse llmResponse = llmAnalysisService.analyzeWithContext(
        rawUrl,
        extractedDomain,
        ruleRisk,
        ruleScore,
        ruleReasons,
        emailSubject,
        emailBody
);

    String finalRisk = decideFinalRisk(ruleRisk, llmResponse.getRisk());
    RiskLevel finalRiskLevel = convertRiskLevel(finalRisk);

    double finalScore = Math.max(ruleScore, llmResponse.getScore());

    if (ruleReasons.isEmpty()) {
        ruleReasons.add("특이사항 없음");
    }

    String finalReasonSummary =
        "1차 규칙 분석: " + String.join(", ", ruleReasons)
                + " / LLM 판단: " + llmResponse.getReasonSummary()
                + " / 대응 권고: " + llmResponse.getRecommendation();
    UrlAnalysis analysis = UrlAnalysis.builder()
            .url(url)
            .domain(extractedDomain)
            .sourceType("MAIL_LLM")
            .riskLevel(finalRiskLevel)
            .riskType(finalRiskLevel == RiskLevel.SAFE ? "NONE" : "PHISHING")
            .score(BigDecimal.valueOf(finalScore))
            .reasonSummary(finalReasonSummary)
            .ruleVersion(CURRENT_RULE_VERSION)
            .sslVerified(rawUrl.startsWith("https"))
            .redirectionDepth(0)
            .containsFormInput(false)
            .featuresJson(
        "{"
                + "\"ruleReasons\":\"" + String.join(", ", ruleReasons) + "\","
                + "\"llmRules\":\"" + String.join(", ", llmResponse.getDetectedRules()) + "\","
                + "\"llmRiskOpinion\":\"" + llmResponse.getLlmRiskOpinion() + "\","
                + "\"confidence\":" + llmResponse.getConfidence() + ","
                + "\"falsePositivePossibility\":" + llmResponse.isFalsePositivePossibility() + ","
                + "\"recommendation\":\"" + llmResponse.getRecommendation() + "\""
                + "}"
)
            .analyzedAt(LocalDateTime.now())
            .build();

    UrlAnalysis savedAnalysis = urlAnalysisRepository.save(analysis);

    analysisHistoryRepository.save(
            AnalysisHistory.createHistory(user, savedAnalysis, "MAIL_LLM")
    );

    if (finalRiskLevel == RiskLevel.DANGER || finalRiskLevel == RiskLevel.CRITICAL) {
        createRiskNotification(user, savedAnalysis);
        sendDiscordAlert(savedAnalysis);
    }

    return savedAnalysis;
}
private String determineRiskByFinalScore(double score) {
    if (score >= 70) {
        return "DANGER";
    }

    if (score >= 30) {
        return "WARNING";
    }

    return "SAFE";
}

private String decideFinalRisk(String ruleRisk, String llmRisk) {
    if ("DANGER".equals(ruleRisk)) {
        return "DANGER";
    }

    if ("WARNING".equals(ruleRisk) && "DANGER".equals(llmRisk)) {
        return "DANGER";
    }

    if ("WARNING".equals(ruleRisk)) {
        return "WARNING";
    }

    if ("SAFE".equals(ruleRisk) && "DANGER".equals(llmRisk)) {
        return "WARNING";
    }

    if ("SAFE".equals(ruleRisk) && "WARNING".equals(llmRisk)) {
        return "WARNING";
    }

    return "SAFE";
}

private RiskLevel convertRiskLevel(String risk) {
    if ("DANGER".equals(risk)) {
        return RiskLevel.DANGER;
    }

    if ("WARNING".equals(risk)) {
        return RiskLevel.WARNING;
    }

    return RiskLevel.SAFE;
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