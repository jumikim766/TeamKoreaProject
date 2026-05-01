package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "url_analysis")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 보호
@AllArgsConstructor
public class UrlAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    private Url url; // url_analysis.url_id → urls.url_id FK

    @Column(name = "source_type", nullable = false, length = 20)
    private String sourceType; // 분석 출처: EMAIL / MANUAL 등

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel; // SAFE / SUSPICIOUS / DANGEROUS / CRITICAL

    @Column(name = "risk_type", length = 50)
    private String riskType; // PHISHING / SHORTENER / IP_HOST 등

    @Column(name = "score", nullable = false, precision = 5, scale = 2)
    private BigDecimal score; // 위험 점수

    @Builder.Default
    @Column(name = "ssl_verified")
    private Boolean sslVerified = false; // SSL 인증서 검증 여부

    @Builder.Default
    @Column(name = "redirection_depth")
    private Integer redirectionDepth = 0; // 리다이렉션 횟수

    @Builder.Default
    @Column(name = "contains_form_input")
    private Boolean containsFormInput = false; // 입력 폼 포함 여부

    @Column(name = "reason_summary", length = 1000)
    private String reasonSummary; // 분석 결과 요약

    @Column(name = "features_json", columnDefinition = "json")
    private String featuresJson; // 분석 특징값 JSON 문자열

    @Column(name = "rule_version", nullable = false, length = 50)
    private String ruleVersion; // 적용 룰 버전

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt; // 실제 분석 시각

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // DB 저장 시각

    // 기존 서비스 코드 호환용 생성자
    public UrlAnalysis(
            Url url,
            String sourceType,
            RiskLevel riskLevel,
            String riskType,
            BigDecimal score,
            Boolean sslVerified,
            Integer redirectionDepth,
            Boolean containsFormInput,
            String reasonSummary,
            String featuresJson,
            String ruleVersion,
            LocalDateTime analyzedAt
    ) {
        this.url = url;
        this.sourceType = sourceType;
        this.riskLevel = riskLevel;
        this.riskType = riskType;
        this.score = score;
        this.sslVerified = sslVerified;
        this.redirectionDepth = redirectionDepth;
        this.containsFormInput = containsFormInput;
        this.reasonSummary = reasonSummary;
        this.featuresJson = featuresJson;
        this.ruleVersion = ruleVersion;
        this.analyzedAt = analyzedAt;
    }

    @PrePersist
    public void prePersist() {
        // 분석 시각이 비어 있으면 현재 시간으로 저장
        if (this.analyzedAt == null) {
            this.analyzedAt = LocalDateTime.now();
        }

        // 저장 시각이 비어 있으면 현재 시간으로 저장
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        // builder 또는 생성자에서 null이 들어온 경우 기본값 보정
        if (this.sslVerified == null) {
            this.sslVerified = false;
        }
        if (this.redirectionDepth == null) {
            this.redirectionDepth = 0;
        }
        if (this.containsFormInput == null) {
            this.containsFormInput = false;
        }
    }
}