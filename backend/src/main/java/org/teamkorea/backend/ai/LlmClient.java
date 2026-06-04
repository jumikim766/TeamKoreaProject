package org.teamkorea.backend.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class LlmClient {

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    private final RestClient restClient = RestClient.create();

    public String call(String prompt) {

        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("OpenAI API KEY가 설정되지 않아 fallback 설명을 사용합니다.");
            return "OpenAI API 키가 설정되지 않아 규칙 기반 분석 결과를 우선 참고해 주세요.";
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", "당신은 피싱 URL을 분석하는 보안 전문가입니다."),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.2
            );
String cleanApiKey = apiKey.trim().replace("\"", "");

            Map response = restClient.post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + cleanApiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            List choices = (List) response.get("choices");
            Map firstChoice = (Map) choices.get(0);
            Map message = (Map) firstChoice.get("message");

            return message.get("content").toString();

        } catch (Exception e) {
            System.out.println("OpenAI API 호출 실패: " + e.getMessage());
            return "LLM 분석 중 오류가 발생했습니다. 규칙 기반 분석 결과를 우선 참고해 주세요.";
        }
    }
}