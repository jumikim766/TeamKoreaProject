package org.teamkorea.backend.ai;

import org.springframework.stereotype.Component;

@Component
public class LlmClient {

    public String call(String prompt) {
        System.out.println("LLM 요청: " + prompt);

        return "이 URL은 피싱 위험 가능성이 있습니다.";
    }
}