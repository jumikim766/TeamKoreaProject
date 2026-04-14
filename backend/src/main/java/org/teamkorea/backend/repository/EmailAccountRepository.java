package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.EmailAccount;

import java.util.List;

public interface EmailAccountRepository extends JpaRepository<EmailAccount, Long> {
    List<EmailAccount> findByUserUserId(Long userId);
}