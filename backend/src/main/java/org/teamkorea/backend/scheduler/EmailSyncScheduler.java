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

    // 자동 sync는 너무 자주 돌리면 IMAP 서버 차단 위험과 서버 부하가 커짐
    // 30분 간격으로 자동 동기화 실행
    @Scheduled(fixedDelay = 1_800_000)
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