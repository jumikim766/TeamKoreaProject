package org.teamkorea.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.teamkorea.backend.domain.Email;
import org.teamkorea.backend.domain.EmailAccount;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailRepository extends JpaRepository<Email, Long> {

    Optional<Email> findByMessageUid(String messageUid);

    boolean existsByMessageUid(String messageUid);

    List<Email> findAllByAccount(EmailAccount account);

    List<Email> findAllByAccountAndReceivedAtAfter(
            EmailAccount account,
            LocalDateTime receivedAt
    );

    Page<Email> findByAccount_AccountId(Long accountId, Pageable pageable);

    // 목록 조회용 fetch join
    @Query("SELECT e FROM Email e JOIN FETCH e.account")
    Page<Email> findAllWithAccount(Pageable pageable);

    // 상세 조회용 fetch join
    @Query("SELECT e FROM Email e JOIN FETCH e.account WHERE e.emailId = :emailId")
    Optional<Email> findByIdWithAccount(Long emailId);
}