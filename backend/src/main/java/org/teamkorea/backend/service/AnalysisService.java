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

    private static final String CURRENT_RULE_VERSION = "ruleset-2.0.0"; // 스펙 변경에 따른 버전 업

    // 정규식 및 비교용 상수 리스트
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
    );

    private static final List<String> SUSPICIOUS_TLDS = Arrays.asList(
            ".xyz", ".top", ".club", ".biz", ".tk", ".info", ".ml", ".ga", ".cf", ".gq"
    );

    private static final List<String> SHORTENER_DOMAINS = Arrays.asList(
            "bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly", "is.gd", "buff.ly"
    );

    /**
     * URL 분석 실행 및 저장 (기획서 1차 규칙 기반 반영)
     */
    public UrlAnalysis analyzeAndSave(Long userId, Long urlId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new IllegalArgumentException("URL을 찾을 수 없습니다."));

        String rawUrl = url.getNormalizedUrl();
        String extractedDomain = extractDomain(rawUrl);
        String lowerUrl = rawUrl.toLowerCase();

        int riskScore = 0;
        List<String> reasonList = new ArrayList<>();

        // 1. 도메인 신뢰도 기반 탐지 (블랙리스트/화이트리스트 최우선 처리)
        DomainReputation reputation = domainReputationRepository.findByDomain(extractedDomain).orElse(null);

        if (reputation != null && Boolean.TRUE.equals(reputation.getIsBlacklisted())) {
            riskScore = 100;
            reasonList.add("기존 악성 도메인 (블랙리스트)");
        } else if (reputation != null && Boolean.TRUE.equals(reputation.getIsWhitelisted())) {
            riskScore = 0;
            reasonList.add("신뢰 가능한 도메인 (화이트리스트)");
        } else {
            // ==========================================
            // [1] URL 구조 기반 탐지 스펙 반영
            // ==========================================
            
            // 1-1. IP 주소 직접 사용 (+50)
            if (IP_PATTERN.matcher(extractedDomain).matches()) {
                riskScore += 50;
                reasonList.add("도메인 대신 IP 주소 사용");
            }

            // 1-2. HTTPS 미사용 (+20)
            if (lowerUrl.startsWith("http://")) {
                riskScore += 20;
                reasonList.add("암호화되지 않은 연결 사용(http)");
            }

            // 1-3. URL 길이 과다 (+20) - 임의 기준 75자 초과 시
            if (rawUrl.length() > 75) {
                riskScore += 20;
                reasonList.add("비정상적으로 긴 URL");
            }

            // 1-4. 하이픈(-) 과다 사용 (+15) - 3개 이상 시
            if (extractedDomain.chars().filter(ch -> ch == '-').count() >= 3) {
                riskScore += 15;
                reasonList.add("브랜드 사칭 의심 (하이픈 과다)");
            }

            // 1-5. 서브도메인 과다 사용 (+15) - 점(.)이 3개 이상 시
            if (extractedDomain.chars().filter(ch -> ch == '.').count() >= 3) {
                riskScore += 15;
                reasonList.add("서브도메인 과다 사용 (위장 의심)");
            }

            // 1-6. 단축 URL 사용 (+30)
            if (SHORTENER_DOMAINS.stream().anyMatch(extractedDomain::equalsIgnoreCase)) {
                riskScore += 30;
                reasonList.add("단축 URL 사용 (목적지 숨김 의심)");
            }

            // 1-7. 의심 TLD 사용 (+25)
            if (SUSPICIOUS_TLDS.stream().anyMatch(extractedDomain::endsWith)) {
                riskScore += 25;
                reasonList.add("의심 TLD 사용");
            }

            // 1-8. 도메인 평판 저하 (+15)
            if (reputation != null && reputation.getTrustScoreValue() < 40.0) {
                riskScore += 15;
                reasonList.add("신뢰도 낮은 도메인");
            }

            // ==========================================
            // [2] 피싱 키워드 기반 탐지 스펙 반영
            // ==========================================
            if (lowerUrl.contains("login")) { riskScore += 20; reasonList.add("login 유도 키워드"); }
            if (lowerUrl.contains("verify")) { riskScore += 15; reasonList.add("인증(verify) 사칭 키워드"); }
            if (lowerUrl.contains("password")) { riskScore += 20; reasonList.add("비밀번호 입력 유도 키워드"); }
            if (lowerUrl.contains("account")) { riskScore += 15; reasonList.add("계정(account) 탈취 키워드"); }
            if (lowerUrl.contains("secure")) { riskScore += 15; reasonList.add("안전 사이트(secure) 위장 키워드"); }
            if (lowerUrl.matches(".*(bank|pay|billing|confirm|update).*")) {
                riskScore += 20; 
                reasonList.add("금융/결제/인증 사칭 키워드 포함");
            }
        }

        // 점수 보정 최댓값 100점 제한
        riskScore = Math.min(riskScore, 100);

        // ==============================================================
        // 📊 최종 위험도 분류 단계 (기획서 매핑: 0~29 / 30~69 / 70~100)
        // ==============================================================
        RiskLevel riskLevel;
        if (riskScore >= 100) {
            riskLevel = RiskLevel.CRITICAL; // 내부 시스템용 최고 등급
        } else if (riskScore >= 70) {
            riskLevel = RiskLevel.DANGER;   // 70 ~ 99
        } else if (riskScore >= 30) {
            riskLevel = RiskLevel.WARNING;  // 30 ~ 69
        } else {
            riskLevel = RiskLevel.SAFE;     // 0 ~ 29
        }

        // 사유 포맷팅 (DB 짤림 방지를 위해 글자 수 안전장치 추가)
        if (reasonList.isEmpty()) {
            reasonList.add(riskLevel == RiskLevel.SAFE ? "특별한 위험 요소가 발견되지 않은 정상 URL" : "기타 미세 위험 요인 탐지");
        }
        String finalReasonSummary = String.join(" / ", reasonList);
        if (finalReasonSummary.length() > 200) {
            finalReasonSummary = finalReasonSummary.substring(0, 197) + "...";
        }

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
                .featuresJson("{}") // LLM 2차 검증을 위해 비워둠
                .analyzedAt(LocalDateTime.now())
                .build();

        UrlAnalysis savedAnalysis = urlAnalysisRepository.saveAndFlush(analysis);
        
        // 3. 히스토리 저장
        //analysisHistoryRepository.save(AnalysisHistory.createHistory(user, savedAnalysis, "MAIL"));

        // 4. DANGER(70점) 이상일 경우 실시간 알림 트리거 활성화
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

    System.out.println("=== URL_ANALYSIS 저장 직전 ===");

UrlAnalysis savedAnalysis = urlAnalysisRepository.saveAndFlush(analysis);

System.out.println("=== URL_ANALYSIS 저장 완료 ===");

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
                .message("수신된 메일에서 피싱 사이트일 가능성이 높은 URL이 발견되었습니다.")
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

            // 디스코드 알림 시 DANGER/CRITICAL 통합 처리 방어
            String levelKor = analysis.getRiskLevel() == RiskLevel.CRITICAL ? "심각" : analysis.getRiskLevel().getDescription();

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