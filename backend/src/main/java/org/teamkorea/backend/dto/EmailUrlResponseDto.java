package org.teamkorea.backend.dto;

import java.math.BigDecimal;

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
    private String reasonSummary;
private BigDecimal score;
}