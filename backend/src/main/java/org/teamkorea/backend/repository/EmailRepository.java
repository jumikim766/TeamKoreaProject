package org.teamkorea.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.teamkorea.backend.domain.Email;
import org.teamkorea.backend.domain.EmailAccount;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailRepository extends JpaRepository<Email, Long> {

    // IMAP 중복 방지
    Optional<Email> findByMessageUid(String messageUid);

    boolean existsByMessageUid(String messageUid);

    // (사용 안함) 전체 조회
    List<Email> findAllByAccount(EmailAccount account);

    // (사용 안함) 특정 시점 이후 조회
    List<Email> findAllByAccountAndReceivedAtAfter(
            EmailAccount account,
            LocalDateTime receivedAt
    );

    // 특정 계정 기준 페이징 조회
    Page<Email> findByAccount_AccountId(Long accountId, Pageable pageable);

    // 이메일 상세 조회 (account 같이 가져오기)
    @Query("SELECT e FROM Email e JOIN FETCH e.account WHERE e.emailId = :emailId")
    Optional<Email> findByIdWithAccount(@Param("emailId") Long emailId);

    // 현재 로그인한 사용자의 전체 이메일 목록 조회
    Page<Email> findByAccount_User_UserId(Long userId, Pageable paseable);

    // 특정 이메일 계정(accountId)이 현재 로그인한 사용자의 것인지 확인하면서 이메일 조회
    Page<Email> findByAccount_AccountIdAndAccount_User_UserId(
        Long accountId,
        Long userId,
        Pageable pageable
    );

    // 현재 로그인한 사용자의 이메일 목록 조회 + 검색 조건 처리
    @Query("""
        SELECT e
        FROM Email e
        JOIN e.account a
        JOIN a.user u
        WHERE u.userId = :userId
          AND (:accountId IS NULL OR a.accountId = :accountId)
          AND (
                :keyword IS NULL
                OR LOWER(e.senderName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(e.senderEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(e.subject) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
          AND (:receivedAtFrom IS NULL OR e.receivedAt >= :receivedAtFrom)
          AND (:receivedAtTo IS NULL OR e.receivedAt <= :receivedAtTo)
        ORDER BY e.receivedAt DESC
        """)
    Page<Email> searchEmailByUser(
        @Param("userId") Long userId,
        @Param("accountId") Long accountId,
        @Param("keyword") String keyword,
        @Param("receivedAtFrom") LocalDateTime receivedAtFrom,
        @Param("receivedAtTo") LocalDateTime receivedAtTo,
        Pageable pageable
    );
}