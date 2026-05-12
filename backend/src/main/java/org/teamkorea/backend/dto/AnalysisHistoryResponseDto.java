package org.teamkorea.backend.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisHistoryResponseDto {
    private Long historyId;
    private Long analysisId;
    private String url;          // normalizedUrl [cite: 247]
    private String riskLevel;    // DANGER, SAFE 등 [cite: 138]
    private String source;       // WEB, EMAIL 등 [cite: 140]
    private String createdAt;    // ISO 8601 형식 [cite: 145]
}