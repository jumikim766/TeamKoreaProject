package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "domain_reputation")
public class DomainReputation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reputation_id")
    private Long reputationId;

    @Column(name = "domain", nullable = false, unique = true, length = 255)
    private String domain;

    @Column(name = "trust_score", nullable = false)
    private Integer trustScore = 50;

    @Column(name = "is_whitelisted")
    private Boolean isWhitelisted = false;

    @Column(name = "is_blacklisted")
    private Boolean isBlacklisted = false;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    public DomainReputation(String domain, Integer trustScore, Boolean isWhitelisted, Boolean isBlacklisted) {
        this.domain = domain;
        this.trustScore = trustScore;
        this.isWhitelisted = isWhitelisted;
        this.isBlacklisted = isBlacklisted;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void onCreate() {
        if (this.lastUpdatedAt == null) {
            this.lastUpdatedAt = LocalDateTime.now();
        }
        if (this.trustScore == null) {
            this.trustScore = 50;
        }
        if (this.isWhitelisted == null) {
            this.isWhitelisted = false;
        }
        if (this.isBlacklisted == null) {
            this.isBlacklisted = false;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }
}