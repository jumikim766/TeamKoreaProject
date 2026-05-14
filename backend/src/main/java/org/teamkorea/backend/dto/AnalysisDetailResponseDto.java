package org.teamkorea.backend.dto;

import lombok.*;
import org.teamkorea.backend.domain.UrlAnalysis;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisDetailResponseDto {
    private Long analysisId; 
    private Long urlId;
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
    private String analyzedAt; // 명세서 규격에 맞게 String으로 관리 

    /**
     * [수정 포인트] 파라미터 오타를 수정하였습니다.
     * 엔티티를 DTO로 변환하는 정적 메서드입니다.
     */
    public static AnalysisDetailResponseDto from(UrlAnalysis analysis) {
        return AnalysisDetailResponseDto.builder()
                .analysisId(analysis.getAnalysisId())
                .urlId(analysis.getUrl().getUrlId()) // DB 최종 코드 url_id 매핑 
                .normalizedUrl(analysis.getUrl().getNormalizedUrl()) // 
                .domain(analysis.getUrl().getDomain()) // 
                .riskLevel(analysis.getRiskLevel().name()) // DANGER 등 
                .riskType(analysis.getRiskType()) // [cite: 116]
                .score(analysis.getScore()) // [cite: 116]
                .reasonSummary(analysis.getReasonSummary()) // [cite: 109]
                .comment("자동 분석 시스템에 의해 생성된 결과입니다.")
                .sslVerified(analysis.getSslVerified())
                .redirectionDepth(analysis.getRedirectionDepth())
                .containsFormInput(analysis.getContainsFormInput())
                .featuresJson(analysis.getFeaturesJson())
                .ruleVersion(analysis.getRuleVersion())
                .analyzedAt(analysis.getAnalyzedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) // ISO 8601 형식 
                .build();
    }
}