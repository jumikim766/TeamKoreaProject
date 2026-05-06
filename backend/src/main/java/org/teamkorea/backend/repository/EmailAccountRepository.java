package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.EmailAccount;
import org.teamkorea.backend.domain.User;

import java.util.List;
import java.util.Optional;

public interface EmailAccountRepository extends JpaRepository<EmailAccount, Long> {

    boolean existsByUserAndEmail(User user, String email);

    List<EmailAccount> findAllByUser(User user);

    // email_accounts에서 active=true인 계정만 (자동 동기화 대상)
    List<EmailAccount> findByActiveTrue();

    Optional<EmailAccount> findByAccountIdAndUser(Long accountId, User user);
}