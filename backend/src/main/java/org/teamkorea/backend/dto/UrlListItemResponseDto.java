package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UrlListItemResponseDto {

    private Long urlId;
    private String normalizedUrl;
    private String domain;
    private String riskLevel;
    private Boolean isAnalyzed;
    private LocalDateTime createdAt;
}