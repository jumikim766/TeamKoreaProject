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
    private Integer trustScore = 50; // 기본 신뢰 점수

    @Builder.Default
    @Column(name = "is_whitelisted", nullable = false) // NULL 방지
    private Boolean isWhitelisted = false;

    @Builder.Default
    @Column(name = "is_blacklisted", nullable = false) // NULL 방지
    private Boolean isBlacklisted = false;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    // 기존 코드 호환용 생성자 유지
    public DomainReputation(String domain, Integer trustScore, Boolean isWhitelisted, Boolean isBlacklisted) {
        this.domain = domain;
        this.trustScore = trustScore;
        this.isWhitelisted = isWhitelisted;
        this.isBlacklisted = isBlacklisted;
        this.lastUpdatedAt = LocalDateTime.now(); // 생성 시점 기록
    }

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        // 생성 시 값 보정
        if (this.lastUpdatedAt == null) this.lastUpdatedAt = now;
        if (this.trustScore == null) this.trustScore = 50;
        if (this.isWhitelisted == null) this.isWhitelisted = false;
        if (this.isBlacklisted == null) this.isBlacklisted = false;
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdatedAt = LocalDateTime.now(); // 수정 시 자동 갱신
    }
}