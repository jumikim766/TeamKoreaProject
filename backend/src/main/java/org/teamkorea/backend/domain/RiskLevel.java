package org.teamkorea.backend.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RiskLevel {
    SAFE("안전"),
    WARNING("주의"),
    DANGER("위험"),     // 사용자 노출 시 통일되는 등급
    CRITICAL("심각");   // 내부 분석용 최고 위험 등급

    private final String description;
}