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

        if (safeBody.length() > 800) {
            safeBody = safeBody.substring(0, 800);
        }

        String safeRules = detectedRules == null || detectedRules.isEmpty()
                ? "없음"
                : String.join(", ", detectedRules);

        return """
                당신은 피싱 URL과 악성 URL을 분석하는 보안 전문가입니다.
                당신의 역할은 1차 규칙 기반 분석 결과를 그대로 따르는 것이 아니라,
                URL 구조, 도메인 특징, 메일 문맥, 사용자 행동 유도 여부를 함께 검토하여
                최종 위험도 의견을 제시하는 2차 검증자입니다.

                최종 위험도는 반드시 SAFE, WARNING, DANGER 중 하나만 사용하세요.

                [분석 대상 URL]
                URL: %s
                도메인: %s

                [1차 규칙 기반 분석 결과]
                1차 위험도: %s
                1차 점수: %.1f
                탐지된 규칙: %s

                [메일 문맥]
                제목: %s
                본문 일부: %s

                [중요 판단 원칙]
                1. 단순히 login, verify, account, secure 같은 키워드가 있다는 이유만으로 DANGER로 판단하지 마세요.
                2. google.com, naver.com, kakao.com, apple.com, microsoft.com 등 명확한 공식 도메인의 정상 로그인 주소는 SAFE 또는 WARNING으로 낮게 판단할 수 있습니다.
                3. 단축 URL, HTTP 사용, IP 주소 직접 사용, 과도한 서브도메인, 긴 URL, 특수문자 과다, 브랜드 사칭 가능성이 함께 나타나면 위험도를 높이세요.
                4. 로그인, 비밀번호, 계정 인증, 결제, 배송, 보안 경고, 긴급 조치 유도 문맥이 URL과 함께 나타나면 위험도를 높이세요.
                5. 메일 본문에서 즉시 클릭, 계정 정지, 결제 실패, 본인 인증, 비밀번호 재설정 등을 유도하면 피싱 가능성을 높게 판단하세요.
                6. URL 구조는 의심스럽지만 명확한 악성 근거가 부족하면 DANGER가 아니라 WARNING으로 판단하세요.
                7. 실제 악성 가능성이 높고 사용자가 클릭하거나 정보를 입력하면 피해가 발생할 수 있는 경우에만 DANGER로 판단하세요.
                8. 판단이 애매하면 SAFE로 낮추지 말고 WARNING을 사용하세요.
                9. 사용자가 보는 위험도는 3단계이므로 UNKNOWN, LOW, MEDIUM, HIGH 같은 표현은 사용하지 마세요.
                10. 과탐 가능성이 있으면 falsePositivePossibility를 true로 설정하세요.

                [SAFE 기준]
                - 공식 도메인으로 보이며 URL 구조가 자연스러움
                - HTTPS 사용
                - 단축 URL, IP 주소, 과도한 서브도메인, 특수문자 남용이 없음
                - 개인정보 입력이나 긴급 조치 유도 문맥이 없음
                - login, account 같은 단어가 있어도 공식 서비스의 정상 기능으로 보임

                [WARNING 기준]
                - 단축 URL을 사용함
                - HTTP를 사용함
                - login, verify, account, secure, update, billing 등의 키워드가 포함됨
                - URL 길이가 길거나 서브도메인이 많음
                - 도메인에 하이픈, 숫자, 비정상 문자열이 많음
                - 메일 문맥상 클릭 유도나 인증 유도가 있으나 악성이라고 단정하기 어려움
                - 정상 URL일 가능성과 피싱 가능성이 모두 존재함

                [DANGER 기준]
                - IP 주소로 직접 접속하게 함
                - 단축 URL과 로그인/계정/인증/결제 키워드가 함께 나타남
                - 유명 브랜드나 기관을 사칭하는 도메인으로 보임
                - 계정 정지, 결제 실패, 보안 경고 등으로 긴급 행동을 유도함
                - 비밀번호, 카드 정보, 인증번호 등 민감정보 입력을 유도할 가능성이 큼
                - URL 구조가 매우 비정상적이고 사용자가 접속하면 피해 가능성이 높음

                [출력 규칙]
                반드시 아래 JSON 형식으로만 응답하세요.
                JSON 앞뒤로 설명 문장을 절대 추가하지 마세요.
                문자열 값 안에는 줄바꿈을 넣지 마세요.
                confidence는 0.0부터 1.0 사이 숫자로 작성하세요.
                llmRiskOpinion은 SAFE, WARNING, DANGER 중 하나만 작성하세요.

                {
                  "llmRiskOpinion": "SAFE",
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
                        safeRules,
                        emailSubject == null ? "" : emailSubject,
                        safeBody
                );
    }
}