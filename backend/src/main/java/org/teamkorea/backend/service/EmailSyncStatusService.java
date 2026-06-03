package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.EmailAccount;
import org.teamkorea.backend.repository.EmailAccountRepository;
import org.teamkorea.backend.exception.BusinessException;
import org.teamkorea.backend.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class EmailSyncStatusService {

    private final EmailAccountRepository emailAccountRepository;

    // sync 성공 상태는 별도 트랜잭션으로 저장
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(Long accountId) {
        EmailAccount account = emailAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "이메일 계정을 찾을 수 없습니다."));

        account.updateSyncSuccess();
    }

    // sync 실패 상태는 기존 sync 트랜잭션이 롤백되어도 저장되도록 분리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long accountId) {
        EmailAccount account = emailAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "이메일 계정을 찾을 수 없습니다."));
        account.updateSyncFailed();
    }

    // 인증 실패 계정은 실패 처리 + 비활성화
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAuthFailedAndDeactivate(Long accountId) {
        EmailAccount account = emailAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "이메일 계정을 찾을 수 없습니다."));

        account.updateSyncFailed();
        account.deactivate();
    }
}