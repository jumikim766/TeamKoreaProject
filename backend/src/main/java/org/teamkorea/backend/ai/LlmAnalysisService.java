package org.teamkorea.backend.ai;

import org.springframework.stereotype.Service;
import org.teamkorea.backend.ai.dto.LlmAnalysisResponse;
import org.teamkorea.backend.ai.dto.LlmVoteResult;
import org.teamkorea.backend.ai.dto.UrlFeatureResult;
import org.teamkorea.backend.ai.prompt.LlmPromptBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LlmAnalysisService {
    private final OpenPhishService openPhishService;
    private final MultiLlmClient multiLlmClient;
    private final UrlFeatureExtractor urlFeatureExtractor;
    
    public LlmAnalysisService(
        MultiLlmClient multiLlmClient,
        UrlFeatureExtractor urlFeatureExtractor,
        OpenPhishService openPhishService
) {
    this.multiLlmClient = multiLlmClient;
    this.urlFeatureExtractor = urlFeatureExtractor;
    this.openPhishService = openPhishService;
}

    public LlmAnalysisResponse analyze(
            String url,
            String domain,
            String risk,
            double baseScore,
            boolean hasIp,
            boolean hasShortUrl
    ) {
        List<String> detectedRules = new ArrayList<>();

        String initialRisk = calculateRisk(url, domain, detectedRules);
        double calculatedScore = calculateScore(url, initialRisk);

        UrlFeatureResult featureResult =
                urlFeatureExtractor.extract(url, domain);
        
                boolean foundInOpenPhish =
        openPhishService.isPhishingUrl(url);

       

        addFeatureRules(featureResult, detectedRules);

        double combinedScore = Math.max(
        calculatedScore,
        featureResult.getSuspiciousScore()
);

if (foundInOpenPhish) {

    combinedScore = Math.max(combinedScore, 70);

    detectedRules.add(
            "OpenPhish 실제 피싱 DB 탐지"
    );
}

combinedScore = Math.min(combinedScore, 100);

        String calculatedRisk = determineFinalRisk(combinedScore);

        String prompt = LlmPromptBuilder.buildPrompt(
                url,
                domain,
                calculatedRisk,
                combinedScore,
                detectedRules,
                null,
                null
        );

        List<LlmVoteResult> votes = multiLlmClient.vote(prompt, calculatedRisk);

        String llmRiskOpinion = decideByMajority(votes, calculatedRisk);
        double confidence = averageConfidence(votes);

        String reason = buildVoteReason(
                votes,
                calculatedRisk,
                detectedRules,
                featureResult
        );

        double finalScore = adjustScoreByLlm(combinedScore, llmRiskOpinion);
        String finalRisk = determineFinalRisk(finalScore);

        return new LlmAnalysisResponse(
                finalRisk,
                reason,
                finalScore,
                detectedRules,
                llmRiskOpinion,
                confidence,
                false,
                createRecommendation(finalRisk)
        );
    }

    public LlmAnalysisResponse analyzeWithContext(
            String url,
            String domain,
            String ruleRisk,
            double ruleScore,
            List<String> detectedRules,
            String emailSubject,
            String emailBody
    ) {
        if (detectedRules == null) {
            detectedRules = new ArrayList<>();
        }

        UrlFeatureResult featureResult =
                urlFeatureExtractor.extract(url, domain);

        addFeatureRules(featureResult, detectedRules);

        double combinedScore = Math.max(
                ruleScore,
                featureResult.getSuspiciousScore()
        );

        String combinedRisk = determineFinalRisk(combinedScore);

        String prompt = LlmPromptBuilder.buildPrompt(
                url,
                domain,
                combinedRisk,
                combinedScore,
                detectedRules,
                emailSubject,
                emailBody
        );

        List<LlmVoteResult> votes = multiLlmClient.vote(prompt, combinedRisk);

        String llmRiskOpinion = decideByMajority(votes, combinedRisk);
        double confidence = averageConfidence(votes);

        boolean falsePositivePossibility =
                "SAFE".equals(llmRiskOpinion) && !"SAFE".equals(combinedRisk);

        String reason = buildVoteReason(
                votes,
                combinedRisk,
                detectedRules,
                featureResult
        );

        double finalScore = adjustScoreByLlm(combinedScore, llmRiskOpinion);
        String finalRisk = determineFinalRisk(finalScore);

        String recommendation = createRecommendation(finalRisk);

        return new LlmAnalysisResponse(
                finalRisk,
                reason,
                finalScore,
                detectedRules,
                llmRiskOpinion,
                confidence,
                falsePositivePossibility,
                recommendation
        );
    }

    private void addFeatureRules(
            UrlFeatureResult featureResult,
            List<String> detectedRules
    ) {
        if (featureResult == null || detectedRules == null) {
            return;
        }

        if (featureResult.isHasIpAddress()) {
            detectedRules.add("Feature: IP 주소 사용");
        }

        if (featureResult.isHasPunycode()) {
            detectedRules.add("Feature: Punycode 사용");
        }

        if (featureResult.getDomainLength() >= 30) {
            detectedRules.add("Feature: 도메인 길이 과다");
        }

        if (featureResult.getHyphenCount() >= 2) {
            detectedRules.add("Feature: 하이픈 과다");
        }

        if (featureResult.getDotCount() >= 3) {
            detectedRules.add("Feature: 서브도메인 과다");
        }

        if (featureResult.getDigitCount() >= 3) {
            detectedRules.add("Feature: 숫자 과다");
        }

        if (featureResult.isHasSuspiciousKeyword()) {
            detectedRules.add("Feature: 의심 키워드 포함");
        }

        if (featureResult.isHasSuspiciousTld()) {
            detectedRules.add("Feature: 의심 TLD 사용");
        }
    }

    private String decideByMajority(List<LlmVoteResult> votes, String ruleRisk) {
        if (votes == null || votes.isEmpty()) {
            return ruleRisk;
        }

        long dangerCount = votes.stream()
                .filter(LlmVoteResult::isSuccess)
                .filter(v -> "DANGER".equals(v.getRisk()))
                .count();

        long warningCount = votes.stream()
                .filter(LlmVoteResult::isSuccess)
                .filter(v -> "WARNING".equals(v.getRisk()))
                .count();

        long safeCount = votes.stream()
                .filter(LlmVoteResult::isSuccess)
                .filter(v -> "SAFE".equals(v.getRisk()))
                .count();

        if (dangerCount >= 2) {
            return "DANGER";
        }

        if (dangerCount + warningCount >= 2) {
            return "WARNING";
        }

        if (safeCount >= 2) {
            return "SAFE";
        }

        return ruleRisk;
    }

    private double averageConfidence(List<LlmVoteResult> votes) {
        if (votes == null || votes.isEmpty()) {
            return 0.5;
        }

        return votes.stream()
                .filter(LlmVoteResult::isSuccess)
                .mapToDouble(LlmVoteResult::getConfidence)
                .average()
                .orElse(0.5);
    }

    private String buildVoteReason(
            List<LlmVoteResult> votes,
            String ruleRisk,
            List<String> detectedRules,
            UrlFeatureResult featureResult
    ) {
        String ruleReason = createFallbackReason(ruleRisk, detectedRules);
        String featureSummary = buildFeatureSummary(featureResult);

        if (votes == null || votes.isEmpty()) {
            return ruleReason + " " + featureSummary;
        }

        String voteSummary = votes.stream()
                .map(v -> v.getProvider() + "=" + v.getRisk())
                .collect(Collectors.joining(", "));

        String detailReason = votes.stream()
                .filter(LlmVoteResult::isSuccess)
                .map(v -> "[" + v.getProvider() + "] " + v.getReason())
                .collect(Collectors.joining(" "));

        if (detailReason.isBlank()) {
            detailReason = ruleReason;
        }

        return "규칙 기반 판단: " + ruleRisk
                + ". LLM 다수결 결과: " + voteSummary
                + ". " + detailReason
                + " " + featureSummary;
    }

    private String buildFeatureSummary(UrlFeatureResult featureResult) {
        if (featureResult == null) {
            return "";
        }

        return "[URL Feature 분석] "
                + "domainLength=" + featureResult.getDomainLength()
                + ", hyphenCount=" + featureResult.getHyphenCount()
                + ", dotCount=" + featureResult.getDotCount()
                + ", digitCount=" + featureResult.getDigitCount()
                + ", ipAddress=" + featureResult.isHasIpAddress()
                + ", punycode=" + featureResult.isHasPunycode()
                + ", suspiciousKeyword=" + featureResult.isHasSuspiciousKeyword()
                + ", suspiciousTld=" + featureResult.isHasSuspiciousTld()
                + ", featureScore=" + featureResult.getSuspiciousScore()
                + ", openPhishChecked=true"
                + ".";
    }

    private double adjustScoreByLlm(double ruleScore, String llmRiskOpinion) {
        if ("DANGER".equals(llmRiskOpinion) && ruleScore < 70) {
            return Math.max(ruleScore, 70);
        }

        if ("WARNING".equals(llmRiskOpinion) && ruleScore < 30) {
            return Math.max(ruleScore, 30);
        }

        if ("SAFE".equals(llmRiskOpinion) && ruleScore >= 70) {
            return 50;
        }

        return ruleScore;
    }

    private String createRecommendation(String risk) {
        if ("DANGER".equals(risk)) {
            return "악성 또는 피싱 가능성이 높으므로 링크를 클릭하지 말고 즉시 삭제하거나 신고하세요.";
        }

        if ("WARNING".equals(risk)) {
            return "의심 요소가 있으므로 개인정보 입력을 피하고 공식 홈페이지나 앱을 통해 직접 접속하세요.";
        }

        return "현재 분석 기준에서는 큰 위험 요소가 발견되지 않았지만, 개인정보 입력 전 주소를 한 번 더 확인하세요.";
    }

    private String calculateRisk(String url, String domain, List<String> detectedRules) {
        if (url == null || url.isBlank()) {
            detectedRules.add("URL 없음");
            return "UNKNOWN";
        }

        String lowerUrl = url.toLowerCase();

        if (lowerUrl.matches(".*http://\\d+\\.\\d+\\.\\d+\\.\\d+.*")) {
            detectedRules.add("IP 주소 사용");
            return "DANGER";
        }

        if (lowerUrl.startsWith("http://")) {
            detectedRules.add("HTTP 사용");
        }

        if (lowerUrl.contains("login")) {
            detectedRules.add("login 키워드 포함");
        }

        if (lowerUrl.contains("verify")) {
            detectedRules.add("verify 키워드 포함");
        }

        if (lowerUrl.contains("password")) {
            detectedRules.add("password 키워드 포함");
        }

        if (lowerUrl.contains("account")) {
            detectedRules.add("account 키워드 포함");
        }

        if (lowerUrl.contains("secure")) {
            detectedRules.add("secure 키워드 포함");
        }

        if (lowerUrl.contains("bit.ly")
                || lowerUrl.contains("tinyurl.com")
                || lowerUrl.contains("t.co")
                || lowerUrl.contains("url.kr")) {
            detectedRules.add("단축 URL 사용");
        }

        if (lowerUrl.contains("bank")
                || lowerUrl.contains("pay")
                || lowerUrl.contains("billing")
                || lowerUrl.contains("update")
                || lowerUrl.contains("confirm")) {
            detectedRules.add("금융/결제/인증 관련 의심 키워드 포함");
        }

        if (domain != null && domain.length() >= 30) {
            detectedRules.add("도메인 길이 과다");
        }

        if (domain != null && domain.chars().filter(ch -> ch == '-').count() >= 2) {
            detectedRules.add("하이픈 과다 사용");
        }

        if (domain != null && domain.split("\\.").length >= 4) {
            detectedRules.add("서브도메인 과다 사용");
        }

        if (domain != null &&
                (domain.contains("naver.com")
                        || domain.contains("google.com")
                        || domain.contains("kakao.com"))) {
            detectedRules.add("신뢰 가능한 도메인");
            return "SAFE";
        }

        if (!detectedRules.isEmpty()) {
            return "WARNING";
        }

        return "SAFE";
    }

    private String createFallbackReason(String risk, List<String> detectedRules) {
        if (detectedRules == null || detectedRules.isEmpty()) {
            return "현재 규칙 기반 분석에서는 특별한 위험 요소가 발견되지 않았습니다.";
        }

        String rules = String.join(", ", detectedRules);

        if ("DANGER".equals(risk)) {
            return "이 URL은 " + rules + " 요소가 탐지되어 피싱 또는 악성 사이트일 가능성이 높습니다.";
        }

        if ("WARNING".equals(risk)) {
            return "이 URL은 " + rules + " 요소가 탐지되어 계정 정보 입력 유도나 피싱 가능성에 주의가 필요합니다.";
        }

        if ("SAFE".equals(risk)) {
            return "이 URL은 " + rules + " 기준에서 비교적 신뢰 가능한 주소로 판단됩니다.";
        }

        return "URL 정보를 충분히 분석할 수 없어 추가 확인이 필요합니다.";
    }

    private double calculateScore(String url, String risk) {
        double calculatedScore = 0;

        if (url == null || url.isBlank()) {
            return calculatedScore;
        }

        String lowerUrl = url.toLowerCase();

        if (lowerUrl.startsWith("http://")) {
            calculatedScore += 20;
        }

        if (lowerUrl.contains("login")) {
            calculatedScore += 20;
        }

        if (lowerUrl.contains("verify")) {
            calculatedScore += 15;
        }

        if (lowerUrl.contains("password")) {
            calculatedScore += 20;
        }

        if (lowerUrl.contains("bit.ly")
                || lowerUrl.contains("tinyurl.com")
                || lowerUrl.contains("t.co")
                || lowerUrl.contains("url.kr")) {
            calculatedScore += 30;
        }

        if (lowerUrl.contains("bank")
                || lowerUrl.contains("pay")
                || lowerUrl.contains("billing")
                || lowerUrl.contains("update")
                || lowerUrl.contains("confirm")) {
            calculatedScore += 20;
        }

        if (lowerUrl.matches(".*\\d+\\.\\d+\\.\\d+\\.\\d+.*")) {
            calculatedScore += 40;
        }

        String extractedDomain = extractDomain(url);

        if (extractedDomain != null && extractedDomain.length() >= 30) {
            calculatedScore += 15;
        }

        if (extractedDomain != null && extractedDomain.chars().filter(ch -> ch == '-').count() >= 2) {
            calculatedScore += 15;
        }

        if (extractedDomain != null && extractedDomain.split("\\.").length >= 4) {
            calculatedScore += 15;
        }

        if ("DANGER".equals(risk)) {
            calculatedScore += 20;
        }

        return Math.min(calculatedScore, 100);
    }

    private String determineFinalRisk(double score) {
        if (score >= 70) {
            return "DANGER";
        }

        if (score >= 30) {
            return "WARNING";
        }

        return "SAFE";
    }

    private String extractDomain(String url) {
        try {
            return java.net.URI.create(url).getHost();
        } catch (Exception e) {
            return null;
        }
    }
}