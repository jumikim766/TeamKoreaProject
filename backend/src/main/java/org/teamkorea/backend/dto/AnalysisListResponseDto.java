package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.teamkorea.backend.domain.AnalysisHistory;
import org.teamkorea.backend.domain.UrlAnalysis;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor 
public class AnalysisListResponseDto {
    private Long analysisId;    
    private String url;         
    private String riskLevel;  
    private String riskType;    // 추가: 'Phishing', 'Malware' 등 위험 유형 노출
    private LocalDateTime analyzedAt;

    /**
     * UrlAnalysis 엔티티를 DTO로 변환하는 생성자
     * 직접 분석 결과(필터링 등)를 조회할 때 사용됩니다.
     */
    public AnalysisListResponseDto(UrlAnalysis analysis) {
        this.analysisId = analysis.getAnalysisId();
        this.url = analysis.getUrl().getNormalizedUrl();
        this.riskLevel = analysis.getRiskLevel().name(); 
        this.riskType = analysis.getRiskType(); // 엔티티의 risk_type 반영
        this.analyzedAt = analysis.getAnalyzedAt();
    }

    /**
     * AnalysisHistory 엔티티를 DTO로 변환하는 생성자
     * 사용자의 히스토리 목록을 조회할 때 사용됩니다.
     */
    public AnalysisListResponseDto(AnalysisHistory history) {
        // 엔티티에서 수정한 필드명 'analysis'를 정확히 참조합니다.
        this.analysisId = history.getAnalysis().getAnalysisId();
        this.url = history.getAnalysis().getUrl().getNormalizedUrl();
        this.riskLevel = history.getAnalysis().getRiskLevel().name();
        this.riskType = history.getAnalysis().getRiskType(); // 히스토리에서도 유형 노출
        this.analyzedAt = history.getAnalysis().getAnalyzedAt();
    }
}