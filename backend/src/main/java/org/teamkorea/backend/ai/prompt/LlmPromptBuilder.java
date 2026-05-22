package org.teamkorea.backend.ai.prompt;

public class LlmPromptBuilder {

    public static String buildPrompt(
            String url,
            String domain,
            String risk,
            double score
    ) {

        return """
                당신은 보안 전문가입니다.

                아래 URL을 분석하여
                왜 위험한지 사용자에게 설명하세요.

                [URL 정보]
                URL: %s
                도메인: %s
                위험도: %s
                위험 점수: %.1f

                설명 조건:
                - 한국어로 설명
                - 2~3문장
                - 일반 사용자도 이해 가능하게 설명
                - 피싱 가능성이 있으면 이유 설명
                """
                .formatted(url, domain, risk, score);
    }
}