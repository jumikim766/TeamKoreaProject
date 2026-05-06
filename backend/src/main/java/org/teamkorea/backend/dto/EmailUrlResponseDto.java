package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EmailUrlResponseDto {

    private Long urlId;
    private String originalUrl;
    private String normalizedUrl;
    private String domain;
    private String riskLevel;
}