package org.teamkorea.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisHistoryResponseDto {
    private Long historyId;      // 이력 PK
    private Long analysisId;     // 분석 결과 PK (상세 보기 이동용)
    private String url;          // 분석된 URL (정규화된 URL)
    private String riskLevel;    // 위험 등급 (SAFE, DANGER 등)
    private String source;       // 조회/분석 원천 (MAIL, MANUAL 등)
    private String createdAt;    // 분석 이력 생성 일시 (문자열 포맷)
}