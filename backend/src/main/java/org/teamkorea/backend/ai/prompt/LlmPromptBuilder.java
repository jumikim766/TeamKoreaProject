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
        String safeSubject = emailSubject == null ? "" : emailSubject;
        String safeBody = emailBody == null ? "" : emailBody;

        if (safeSubject.length() > 300) {
            safeSubject = safeSubject.substring(0, 300);
        }

        if (safeBody.length() > 3000) {
            safeBody = safeBody.substring(0, 3000);
        }

        return """
                당신은 피싱 URL과 피싱 메일을 함께 분석하는 보안 전문가입니다.

                아래 정보는 1차 규칙 기반 URL 분석 결과, URL Feature 분석 결과, 실제 피싱 DB 탐지 결과, 메일 문맥입니다.
                LLM은 단독 판단자가 아니라, 1차 분석 결과와 메일 문맥을 함께 검토하는 2차 검증 역할을 수행합니다.

                [URL 정보]
                URL: %s
                도메인: %s

                [1차 분석 결과]
                1차 위험도: %s
                1차 점수: %.1f
                탐지된 규칙: %s

                [메일 정보]
                제목: %s
                본문 일부:
                %s

                [URL 분석 기준]
                - URL 자체가 피싱 또는 악성 URL로 의심되는지 검토
                - 브랜드 사칭, 단축 URL, 리다이렉트, 의심 TLD, 신규 생성 도메인 여부를 검토
                - 공식 도메인처럼 보이더라도 로그인/인증/결제 유도 문맥이 있으면 주의
                - 1차 규칙 분석이 과탐일 가능성이 있는지 판단

                [메일 문맥 분석 기준]
                다음 표현이나 흐름이 있으면 피싱 가능성을 높게 판단하세요.
                - 계정 정지, 계정 제한, 보안 경고
                - 로그인, 인증, 본인 확인, 비밀번호 변경 요구
                - 결제 실패, 송장, 배송, 환불, 카드, 은행 관련 안내
                - 긴급 조치 요구: 즉시, 오늘까지, 24시간 이내, 마지막 경고
                - 링크 클릭 유도: 아래 링크, 버튼 클릭, 확인하기, 로그인하기
                - 개인정보 입력 요구: 아이디, 비밀번호, 카드번호, 인증번호, 주민번호
                - 발신자나 문장이 공식 기관/브랜드를 사칭하는 듯한 표현
                - 맞춤법이 어색하거나 과도하게 불안감을 조성하는 표현

                [판단 기준]
                - URL 위험도와 메일 문맥이 모두 의심스러우면 DANGER
                - URL은 약하지만 메일 문맥이 개인정보 입력이나 긴급 조치를 강하게 유도하면 WARNING 이상
                - 공식 도메인이며 메일 문맥도 자연스럽고 개인정보 입력 유도가 약하면 SAFE 가능
                - 일반 사용자가 이해할 수 있는 한국어 설명을 생성
                - 사용자가 취해야 할 대응 권고 생성

                반드시 아래 JSON 형식으로만 응답하세요.
                다른 문장은 절대 추가하지 마세요.

                {
                  "llmRiskOpinion": "SAFE 또는 WARNING 또는 DANGER",
                  "confidence": 0.0,
                  "falsePositivePossibility": false,
                  "reason": "URL과 메일 문맥을 함께 고려한 판단 이유를 한국어로 2~3문장 작성",
                  "recommendation": "사용자 대응 권고를 한국어로 1문장 작성"
                }
                """
                .formatted(
                        url,
                        domain,
                        ruleRisk,
                        ruleScore,
                        String.join(", ", detectedRules),
                        safeSubject,
                        safeBody
                );
    }
}