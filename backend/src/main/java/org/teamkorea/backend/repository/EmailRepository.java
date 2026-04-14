package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.Email;
import org.teamkorea.backend.domain.EmailAccount;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailRepository extends JpaRepository<Email, Long> {

    // 중복 방지 (IMAP UID 기준)
    Optional<Email> findByMessageUid(String messageUid);

    boolean existsByMessageUid(String messageUid);

    // 특정 계정 이메일 목록 조회
    List<Email> findAllByAccount(EmailAccount account);

    // 최신 동기화 기준 조회
    List<Email> findAllByAccountAndReceivedAtAfter(
            EmailAccount account,
            LocalDateTime receivedAt
    );
}