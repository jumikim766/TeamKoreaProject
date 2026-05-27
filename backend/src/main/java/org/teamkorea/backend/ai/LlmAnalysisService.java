package org.teamkorea.backend.ai;

import org.springframework.stereotype.Service;
import org.teamkorea.backend.ai.dto.LlmAnalysisResponse;
import org.teamkorea.backend.ai.prompt.LlmPromptBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
public class LlmAnalysisService {

    private final LlmClient llmClient;

    public LlmAnalysisService(LlmClient llmClient) {
        this.llmClient = llmClient;
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
        String calculatedRisk = determineFinalRisk(calculatedScore);

        String prompt = LlmPromptBuilder.buildPrompt(
                url,
                domain,
                calculatedRisk,
                calculatedScore,
                detectedRules,
                null,
                null
        );

        String reason = llmClient.call(prompt);

        if (reason == null || reason.contains("LLM 분석 중 오류가 발생했습니다")) {
            reason = createFallbackReason(calculatedRisk, detectedRules);
        }

        return new LlmAnalysisResponse(
                calculatedRisk,
                reason,
                calculatedScore,
                detectedRules,
                calculatedRisk,
                0.5,
                false,
                "의심스러운 링크는 클릭하지 말고 공식 홈페이지나 앱을 통해 직접 접속하세요."
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
        String prompt = LlmPromptBuilder.buildPrompt(
                url,
                domain,
                ruleRisk,
                ruleScore,
                detectedRules,
                emailSubject,
                emailBody
        );

        String llmText = llmClient.call(prompt);

        String llmRiskOpinion = extractJsonString(llmText, "llmRiskOpinion", ruleRisk);
        double confidence = extractJsonDouble(llmText, "confidence", 0.5);
        boolean falsePositivePossibility =
                extractJsonBoolean(llmText, "falsePositivePossibility", false);

        String reason = extractJsonString(
                llmText,
                "reason",
                createFallbackReason(ruleRisk, detectedRules)
        );

        String recommendation = extractJsonString(
                llmText,
                "recommendation",
                "의심스러운 링크는 클릭하지 말고 공식 홈페이지나 앱을 통해 직접 접속하세요."
        );

        double finalScore = ruleScore;

        if ("DANGER".equals(llmRiskOpinion) && ruleScore < 70) {
            finalScore = Math.max(ruleScore, 70);
        } else if ("WARNING".equals(llmRiskOpinion) && ruleScore < 30) {
            finalScore = Math.max(ruleScore, 30);
        }

        String finalRisk = determineFinalRisk(finalScore);

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
private String extractJsonString(String text, String key, String defaultValue) {
    if (text == null || text.isBlank()) {
        return defaultValue;
    }

    try {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "\"" + key + "\"\\s*:\\s*\"([^\"]*)\""
        );

        java.util.regex.Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return defaultValue;
    } catch (Exception e) {
        return defaultValue;
    }
}

private double extractJsonDouble(String text, String key, double defaultValue) {
    if (text == null || text.isBlank()) {
        return defaultValue;
    }

    try {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "\"" + key + "\"\\s*:\\s*([0-9.]+)"
        );

        java.util.regex.Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }

        return defaultValue;
    } catch (Exception e) {
        return defaultValue;
    }
}

private boolean extractJsonBoolean(String text, String key, boolean defaultValue) {
    if (text == null || text.isBlank()) {
        return defaultValue;
    }

    try {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "\"" + key + "\"\\s*:\\s*(true|false)"
        );

        java.util.regex.Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1));
        }

        return defaultValue;
    } catch (Exception e) {
        return defaultValue;
    }
}
    

}