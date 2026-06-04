package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSyncAsyncService {

    private final EmailAccountService emailAccountService;

    @Async
    public void syncEmailsInBackground(Long userId, Long accountId) {
        try {
            emailAccountService.syncEmails(userId, accountId);
        } catch (Exception e) {
            // 백그라운드 작업 실패는 사용자 응답을 막지 않음
            log.error("[EMAIL SYNC ASYNC] 백그라운드 동기화 실패 - userId={}, accountId={}",
                    userId, accountId, e);
        }
    }
}