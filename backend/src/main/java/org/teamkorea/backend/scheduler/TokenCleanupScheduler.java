package org.teamkorea.backend.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.teamkorea.backend.service.AuthService;

@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final AuthService authService;

    // 1시간마다 만료된 Refresh Token을 DB에서 삭제
    @Scheduled(fixedRate = 3600000)
    public void cleanExpiredRefreshTokens() {
        authService.deleteExpiredRefreshTokens();
    }
}