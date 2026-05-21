package org.teamkorea.backend.dto;

import lombok.*;
import org.teamkorea.backend.domain.UrlAnalysis;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisDetailResponseDto {
    private Long analysisId;
    private String url;
    private String domain;
    private String riskLevel;
    private String riskType;
    private BigDecimal score;
    private String reasonSummary;
    private Boolean sslVerified;
    private Integer redirectionDepth;
    private LocalDateTime analyzedAt;

    // 엔티티를 DTO로 바로 변환하는 생성자
    public AnalysisDetailResponseDto(UrlAnalysis analysis) {
        this.analysisId = analysis.getAnalysisId();
        this.url = analysis.getUrl().getNormalizedUrl();
        this.domain = analysis.getDomain();
        this.riskLevel = analysis.getRiskLevel().name();
        this.riskType = analysis.getRiskType();
        this.score = analysis.getScore();
        this.reasonSummary = analysis.getReasonSummary();
        this.sslVerified = analysis.getSslVerified();
        this.redirectionDepth = analysis.getRedirectionDepth();
        this.analyzedAt = analysis.getAnalyzedAt();
    }

    public static AnalysisDetailResponseDto from(UrlAnalysis analysis) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'from'");
    }
}