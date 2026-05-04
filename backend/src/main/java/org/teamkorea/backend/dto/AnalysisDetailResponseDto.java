package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.teamkorea.backend.domain.UrlAnalysis;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisDetailResponseDto {
    private Long analysisId;
    private Long urlId;
    
    // 추가 포인트: API 명세서 [7]번 요구사항 반영
    private String normalizedUrl; 
    private String domain;

    private String riskLevel;   
    private String riskType;   
    private BigDecimal score;
    private String reasonSummary;
    private String comment;     
    private Boolean sslVerified;
    private Integer redirectionDepth;
    private Boolean containsFormInput;
    private String featuresJson;
    private String ruleVersion;
    private LocalDateTime analyzedAt;

    /**
     * UrlAnalysis 엔티티를 상세 DTO로 변환하는 생성자
     */
    public AnalysisDetailResponseDto(UrlAnalysis analysis) {
        this.analysisId = analysis.getAnalysisId();
        this.urlId = analysis.getUrl().getUrlId();
        
        // 추가 포인트: 연관된 Url 엔티티에서 정보를 가져옵니다
        this.normalizedUrl = analysis.getUrl().getNormalizedUrl();
        this.domain = analysis.getUrl().getDomain();

        this.riskLevel = analysis.getRiskLevel().name(); 
        this.riskType = analysis.getRiskType();
        this.score = analysis.getScore();
        this.reasonSummary = analysis.getReasonSummary();
        this.comment = "자동 분석 시스템에 의해 생성된 결과입니다."; 
        this.sslVerified = analysis.getSslVerified();
        this.redirectionDepth = analysis.getRedirectionDepth();
        this.containsFormInput = analysis.getContainsFormInput();
        this.featuresJson = analysis.getFeaturesJson();
        this.ruleVersion = analysis.getRuleVersion();
        this.analyzedAt = analysis.getAnalyzedAt();
    }
}