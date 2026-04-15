package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "url_analysis")
public class UrlAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    private Double riskScore;

    private String riskLevel; // SAFE, SUSPICIOUS, MALICIOUS

    private LocalDateTime analyzedAt;

    @Builder
    public UrlAnalysis(String originalUrl, Double riskScore, String riskLevel) {
        this.originalUrl = originalUrl;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.analyzedAt = LocalDateTime.now();
    }
}
