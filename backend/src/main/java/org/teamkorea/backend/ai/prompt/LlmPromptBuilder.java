package org.teamkorea.backend.ai.prompt;

import java.util.List;

public class LlmPromptBuilder {

    public static String buildPrompt(
            String url,
            String domain,
            String ruleRisk,
            double ruleScore,
            List<String> detectedRules,
            String emailSubject,
            String emailBody
    ) {
        String safeBody = emailBody == null ? "" : emailBody;

        if (safeBody.length() > 500) {
            safeBody = safeBody.substring(0, 500);
        }

        return """
                당신은 피싱 URL을 분석하는 보안 전문가입니다.

                아래 정보는 1차 규칙 기반 분석 결과와 메일 문맥입니다.
                LLM은 단독 판단자가 아니라, 1차 분석 결과를 검토하고 사용자에게 설명하는 2차 검증 역할을 수행합니다.

                [URL 정보]
                URL: %s
                도메인: %s

                [1차 규칙 기반 분석]
                1차 위험도: %s
                1차 점수: %.1f
                탐지된 규칙: %s

                [메일 정보]
                제목: %s
                본문 일부: %s

                [분석 기준]
                - URL 자체가 피싱 또는 악성 URL로 의심되는지 검토
                - 메일 제목과 본문이 계정 인증, 로그인, 결제, 긴급 조치를 유도하는지 검토
                - 1차 규칙 분석이 과탐일 가능성이 있는지 판단
                - 일반 사용자가 이해할 수 있는 한국어 설명 생성
                - 사용자가 취해야 할 대응 권고 생성

                반드시 아래 JSON 형식으로만 응답하세요.
                다른 문장은 절대 추가하지 마세요.

                {
                  "llmRiskOpinion": "SAFE 또는 WARNING 또는 DANGER",
                  "confidence": 0.0,
                  "falsePositivePossibility": false,
                  "reason": "판단 이유를 한국어로 2~3문장 작성",
                  "recommendation": "사용자 대응 권고를 한국어로 1문장 작성"
                }
                """
                .formatted(
                        url,
                        domain,
                        ruleRisk,
                        ruleScore,
                        String.join(", ", detectedRules),
                        emailSubject == null ? "" : emailSubject,
                        safeBody
                );
    }
}