package org.teamkorea.backend.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RiskLevel {

    SAFE("안전"),
    SUSPICIOUS("의심"),
    DANGEROUS("위험"),
    CRITICAL("심각");

    private final String description; // UI 또는 응답 확장용
}