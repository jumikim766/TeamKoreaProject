package org.teamkorea.backend.ai;

import org.springframework.stereotype.Component;
import org.teamkorea.backend.ai.dto.LlmVoteResult;

import java.util.ArrayList;
import java.util.List;

@Component
public class MultiLlmClient {

    private final LlmClient llmClient;

    public MultiLlmClient(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public List<LlmVoteResult> vote(String prompt, String fallbackRisk) {
        List<LlmVoteResult> votes = new ArrayList<>();

        votes.add(callGpt(prompt, fallbackRisk));
        votes.add(LlmVoteResult.fallback("GEMINI", fallbackRisk, "Gemini API 키가 없어 규칙 기반 결과로 대체했습니다."));
        votes.add(LlmVoteResult.fallback("CLAUDE", fallbackRisk, "Claude API 키가 없어 규칙 기반 결과로 대체했습니다."));

        return votes;
    }

    private LlmVoteResult callGpt(String prompt, String fallbackRisk) {
    try {
        System.out.println("=== OpenAI API 호출 시작 ===");

        String response = llmClient.call(prompt);

        System.out.println("=== OpenAI 응답 성공 ===");
        System.out.println(response);

        String risk = extractJsonString(response, "llmRiskOpinion", fallbackRisk);
        double confidence = extractJsonDouble(response, "confidence", 0.5);
        String reason = extractJsonString(response, "reason", "GPT 분석 결과를 기반으로 판단했습니다.");

        return new LlmVoteResult("GPT", risk, confidence, reason, true);

    } catch (Exception e) {
        System.out.println("=== OpenAI 호출 실패 ===");
        e.printStackTrace();

        return LlmVoteResult.fallback("GPT", fallbackRisk, "GPT 호출 실패로 규칙 기반 결과를 사용했습니다.");
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
}