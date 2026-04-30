package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "provider", nullable = false, length = 20)
    private String provider;

    @Column(name = "imap_host", nullable = false, length = 255)
    private String imapHost;

    @Column(name = "imap_port", nullable = false)
    private Integer imapPort;

    @Column(name = "login_id", nullable = false, length = 100)
    private String loginId;

    @Lob
    @Column(name = "secret_enc", nullable = false)
    private byte[] secretEnc;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "last_sync_status", length = 20)
    private String lastSyncStatus;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
}