package org.teamkorea.backend.domain;

public enum ReportStatus {
    RECEIVED("접수됨"),
    REVIEWING("검토 중"),
    COMPLETED("처리 완료");

    private final String description;

    ReportStatus(String description) {
        this.description = description;
    }
}