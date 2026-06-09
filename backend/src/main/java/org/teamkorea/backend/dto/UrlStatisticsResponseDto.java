package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UrlStatisticsResponseDto {
    private long totalCount;
    private long criticalCount;
    private long dangerCount;
    private long warningCount;
    // private long suspiciousCount; ➔ 기획 변경으로 이 부분이 삭제되었습니다!
    private long safeCount;
    private long unanalyzedCount;
}