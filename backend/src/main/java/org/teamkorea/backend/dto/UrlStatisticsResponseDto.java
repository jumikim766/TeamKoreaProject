package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UrlStatisticsResponseDto {

    private long totalCount;
    private long criticalCount;
    private long dangerCount;
    private long cautionCount;
    private long safeCount;
    private long unanalyzedCount;
}