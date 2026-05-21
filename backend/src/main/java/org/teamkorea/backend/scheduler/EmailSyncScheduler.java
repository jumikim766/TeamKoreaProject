package org.teamkorea.backend.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.teamkorea.backend.domain.EmailAccount;
import org.teamkorea.backend.repository.EmailAccountRepository;
import org.teamkorea.backend.service.EmailAccountService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSyncScheduler {

    private final EmailAccountRepository emailAccountRepository;
    private final EmailAccountService emailAccountService;

    // 개발 중에는 자동 동기화가 수동 최초 sync보다 먼저 실행될 수 있어서 잠시 비활성화
    // @Scheduled(fixedDelay = 60_000)
    public void syncActiveEmailAccounts() {
        List<EmailAccount> activeAccounts = emailAccountRepository.findByActiveTrue();

        for (EmailAccount account : activeAccounts) {
            try {
                Long userId = account.getUser().getUserId();
                Long accountId = account.getAccountId();

                emailAccountService.syncEmails(userId, accountId);

            } catch (Exception e) {
                log.error("[EmailSyncScheduler] 계정 동기화 실패 - accountId={}, email={}",
                        account.getAccountId(), account.getEmail(), e);
            }
        }
    }
}