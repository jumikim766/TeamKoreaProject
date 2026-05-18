package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "domain_reputation")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DomainReputation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reputation_id")
    private Long reputationId;

    @Column(name = "domain", nullable = false, unique = true, length = 255)
    private String domain;

    @Builder.Default
    @Column(name = "trust_score", nullable = false)
    private Integer trustScore = 50;

    @Builder.Default
    @Column(name = "is_whitelisted", nullable = false)
    private Boolean isWhitelisted = false;

    @Builder.Default
    @Column(name = "is_blacklisted", nullable = false)
    private Boolean isBlacklisted = false;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    public DomainReputation(String domain, Integer trustScore, Boolean isWhitelisted, Boolean isBlacklisted) {
        this.domain = domain;
        this.trustScore = trustScore != null ? trustScore : 50;
        this.isWhitelisted = isWhitelisted != null ? isWhitelisted : false;
        this.isBlacklisted = isBlacklisted != null ? isBlacklisted : false;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.lastUpdatedAt == null) this.lastUpdatedAt = now;
        if (this.trustScore == null) this.trustScore = 50;
        if (this.isWhitelisted == null) this.isWhitelisted = false;
        if (this.isBlacklisted == null) this.isBlacklisted = false;
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public double getTrustScoreValue() {
        return this.trustScore != null ? this.trustScore.doubleValue() : 50.0;
    }

    // 🚨 IDE 에러 방지를 위해 명시적으로 Getter를 추가했습니다!
    public Boolean getIsWhitelisted() {
        return this.isWhitelisted;
    }

    public Boolean getIsBlacklisted() {
        return this.isBlacklisted;
    }
}