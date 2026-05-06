package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 보호
@AllArgsConstructor
@Builder
public class EmailAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // email_accounts.user_id → users.user_id FK

    @Column(name = "email", nullable = false, length = 100)
    private String email; // 연동한 이메일 주소

    @Column(name = "provider", nullable = false, length = 20)
    private String provider; // GMAIL, NAVER 등 메일 제공자

    @Column(name = "imap_host", nullable = false, length = 255)
    private String imapHost; // IMAP 서버 주소

    @Column(name = "imap_port", nullable = false)
    private Integer imapPort; // IMAP 포트 번호

    @Column(name = "login_id", nullable = false, length = 100)
    private String loginId; // 실제 메일 로그인 ID

    // DB 명세서 기준: secret_enc VARBINARY(1024)
    @Column(name = "secret_enc", nullable = false, columnDefinition = "VARBINARY(1024)")
    private byte[] secretEnc; // 암호화된 앱 비밀번호 또는 토큰

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true; // 기본값 true

    @Column(name = "last_sync_status", length = 20)
    private String lastSyncStatus; // SUCCESS / FAILED

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt; // 마지막 동기화 시각

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 계정 등록 시각

    @PrePersist
    public void prePersist() {
        // 저장 직전에 생성 시간 자동 입력
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        // active 값이 비어 있으면 기본값 true로 저장
        if (this.active == null) {
            this.active = true;
        }
    }

    // 이메일 동기화 성공 시 상태 갱신
    public void updateSyncSuccess() {
        this.lastSyncStatus = "SUCCESS";
        this.lastSyncedAt = LocalDateTime.now();
    }

    // 이메일 동기화 실패 시 상태 갱신
    public void updateSyncFailed() {
        this.lastSyncStatus = "FAILED";
    }
}