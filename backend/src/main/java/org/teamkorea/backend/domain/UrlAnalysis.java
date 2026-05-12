package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "url_analysis")
@Getter 
@Setter // 분석 결과를 생성한 후 상세 내용을 채우거나 수정할 때 필요합니다.
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

    @Column(name = "source_type", nullable = false, length = 20)
    private String sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel; 
    // API 명세서 기준: SAFE, SUSPICIOUS, DANGEROUS, CRITICAL 순으로 권장 [cite: 65, 68, 115]

    @Column(name = "risk_type", length = 50)
    private String riskType; // PHISHING, MALWARE 등 [cite: 115, 139]

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
    private String reasonSummary; // "분석 결과 요약 내용" [cite: 109, 141]

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
        if (this.sslVerified == null) this.sslVerified = false;
        if (this.redirectionDepth == null) this.redirectionDepth = 0;
        if (this.containsFormInput == null) this.containsFormInput = false;
    }

    // 위험 등급인지 확인하는 비즈니스 메서드 (알림 생성 여부 결정 시 활용)
    public boolean isHighRisk() {
        return this.riskLevel == RiskLevel.CRITICAL || this.riskLevel == RiskLevel.DANGER;
    }
}