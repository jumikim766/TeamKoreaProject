package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "domain_reputation")
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
    @Column(name = "is_whitelisted")
    private Boolean isWhitelisted = false;

    @Builder.Default
    @Column(name = "is_blacklisted")
    private Boolean isBlacklisted = false;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @PrePersist
    public void onCreate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }
}