package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "refresh_tokens") // DB 테이블명과 정확히 연결
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 보호
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id") // DB PK 컬럼명과 일치
    private Long tokenId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) // refresh_tokens.user_id → users.user_id FK
    private User user;

    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash; // 원본 refreshToken이 아니라 해시값 저장

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // refreshToken 만료 시각

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // refreshToken 생성 시각

    public RefreshToken(User user, String tokenHash, LocalDateTime expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    public void updateTokenHash(String tokenHash, LocalDateTime expiresAt) {
        // 재로그인 또는 재발급 시 토큰 해시와 만료시간 갱신
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }
}