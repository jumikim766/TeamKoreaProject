package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "url_analysis")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UrlAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    private Url url;

    // [수정] 명세서 필수 항목인 도메인 필드 추가 [cite: 82, 120]
    @Column(name = "domain", nullable = false, length = 255)
    private String domain;

    // [수정] 명세서 [2]에 따른 제공자 구분값 [cite: 236]
    @Column(name = "source_type", nullable = false, length = 20)
    private String sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel; 

    // [수정] 명세서 응답 예시의 'riskType' 반영 [cite: 124, 139]
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.analyzedAt == null) this.analyzedAt = now;
        if (this.createdAt == null) this.createdAt = now;
        if (this.ruleVersion == null) this.ruleVersion = "v1.0"; 
    }
}