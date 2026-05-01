package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.teamkorea.backend.domain.EmailAccount;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAccountResponse {

    private Long accountId;
    private Long userId;
    private String provider;
    private String email;
    private Boolean active;
    private String lastSyncStatus;
    private LocalDateTime lastSyncedAt;
    private LocalDateTime createdAt;

    /**
     * EmailAccount 엔티티를 Response DTO로 변환
     */
    public EmailAccountResponse(EmailAccount account) {
        this.accountId = account.getAccountId();

        // ❗ Lazy 로딩 방어 (null-safe)
        this.userId = (account.getUser() != null) ? account.getUser().getUserId() : null;

        this.provider = account.getProvider();
        this.email = account.getEmail();
        this.active = account.getActive();
        this.lastSyncStatus = account.getLastSyncStatus();
        this.lastSyncedAt = account.getLastSyncedAt();
        this.createdAt = account.getCreatedAt();
    }
}