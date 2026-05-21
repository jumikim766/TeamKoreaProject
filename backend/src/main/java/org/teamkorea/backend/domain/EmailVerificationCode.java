package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EmailVerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long verificationId;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false, length = 30)
    private String purpose;

    @Column(nullable = false)
    private Boolean verified;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (verified == null) {
            verified = false;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // 인증 성공 처리
    public void markVerified() {
        this.verified = true;
    }

    // 이메일 인증번호 사용(인증 완료) 여부 확인
    public boolean isVerified() {
        return verified;
    }

    // 만료 여부 확인
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
}