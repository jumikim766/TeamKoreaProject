package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyUrlItemResponseDto {

    private Long urlId;
    private Long emailId;
    private Long accountId;
    private String senderName;
    private String senderEmail;
    private String emailSubject;
    private String originalUrl;
    private String normalizedUrl;
    private String domain;
    private String riskLevel;
    private String reasonSummary;
    private Boolean isAnalyzed;
    private LocalDateTime receivedAt;
    private LocalDateTime createdAt;
}