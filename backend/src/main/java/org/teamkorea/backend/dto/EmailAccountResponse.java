package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}