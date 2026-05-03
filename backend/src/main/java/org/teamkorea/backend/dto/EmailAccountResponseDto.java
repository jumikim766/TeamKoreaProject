package org.teamkorea.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EmailAccountResponseDto {

    private Long accountId;
    private Long userId;
    private String provider;
    private String email;
    private Boolean active;
    private String lastSyncStatus;
    private LocalDateTime lastSyncedAt;
    private LocalDateTime createdAt;
}