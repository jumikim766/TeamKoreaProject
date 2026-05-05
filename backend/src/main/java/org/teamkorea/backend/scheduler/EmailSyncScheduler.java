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

    // 개발용: 1분마다 자동 동기화 실행 (실제는 시간 조정 필요 5~10m)
    @Scheduled(fixedDelay = 60_000)
    public void syncActiveEmailAccounts() {
        log.info("[EmailSyncScheduler] 자동 이메일 동기화 시작");

        List<EmailAccount> activeAccounts = emailAccountRepository.findByActiveTrue();

        for (EmailAccount account : activeAccounts) {
            try {
                Long userId = account.getUser().getUserId();
                Long accountId = account.getAccountId();

                log.info("[EmailSyncScheduler] 계정 동기화 시작 - accountId={}, email={}",
                        accountId, account.getEmail());

                emailAccountService.syncEmails(userId, accountId);

                log.info("[EmailSyncScheduler] 계정 동기화 완료 - accountId={}", accountId);

            } catch (Exception e) {
                log.error("[EmailSyncScheduler] 계정 동기화 실패 - accountId={}, email={}",
                        account.getAccountId(), account.getEmail(), e);
            }
        }

        log.info("[EmailSyncScheduler] 자동 이메일 동기화 종료");
    }
}