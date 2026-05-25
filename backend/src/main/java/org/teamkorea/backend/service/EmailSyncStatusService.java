package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.EmailAccount;
import org.teamkorea.backend.repository.EmailAccountRepository;

@Service
@RequiredArgsConstructor
public class EmailSyncStatusService {

    private final EmailAccountRepository emailAccountRepository;

    // sync 성공 상태는 별도 트랜잭션으로 저장
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(Long accountId) {
        EmailAccount account = emailAccountRepository.findById(accountId)
                .orElseThrow();

        account.updateSyncSuccess();
    }

    // sync 실패 상태는 기존 sync 트랜잭션이 롤백되어도 저장되도록 분리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long accountId) {
        EmailAccount account = emailAccountRepository.findById(accountId)
                .orElseThrow();

        account.updateSyncFailed();
    }
}