package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder 
@Table(name = "url_analysis")
public class UrlAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    private Url url;

    @Column(name = "source_type", nullable = false, length = 20)
    private String sourceType;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;

    @Column(name = "risk_type", length = 50)
    private String riskType;

    @Column(name = "score", nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Builder.Default
    @Column(name = "ssl_verified")
    private Boolean sslVerified = false;

    @Builder.Default
    @Column(name = "redirection_depth")
    private Integer redirectionDepth = 0;

    @Builder.Default
    @Column(name = "contains_form_input")
    private Boolean containsFormInput = false;

    @Column(name = "reason_summary", length = 1000)
    private String reasonSummary;

    @Column(name = "features_json", columnDefinition = "json")
    private String featuresJson;

    @Column(name = "rule_version", nullable = false, length = 50)
    private String ruleVersion;

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
}