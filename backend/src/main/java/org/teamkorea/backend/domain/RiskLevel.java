package org.teamkorea.backend.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RiskLevel {
    SAFE("안전"),
    SUSPICIOUS("의심"),
    WARNING("주의"),
    DANGER("위험"),
    DANGEROUS("위험함"), // 팀원들 기존 코드 호환용
    CRITICAL("심각");

    private final String description;
}