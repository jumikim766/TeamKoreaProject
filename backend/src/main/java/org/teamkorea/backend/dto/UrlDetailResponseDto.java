package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UrlDetailResponseDto {

    private Long urlId;
    private String senderName;
    private String senderEmail;
    private String originalUrl;
    private String normalizedUrl;
    private String domain;
    private String riskLevel;
    private String reasonSummary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}