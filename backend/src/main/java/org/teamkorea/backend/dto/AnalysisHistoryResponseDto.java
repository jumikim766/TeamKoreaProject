package org.teamkorea.backend.dto;

import lombok.*;

@Getter
@Setter // 서비스에서 데이터를 매핑할 때 필요할 수 있습니다
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisHistoryResponseDto {
    private Long historyId;      // 분석 이력 PK
    private Long analysisId;     // 연관된 분석 결과 PK (상세 페이지 이동용)
    private String url;          // 분석된 정규화 URL (normalizedUrl)
    private String riskLevel;    // 위험 등급 (DANGER, SAFE 등)
    private String source;       // 분석 요청 경로 (WEB, EMAIL 등)
    private String createdAt;    // 분석 일시 (ISO 8601 형식 문자열)
}