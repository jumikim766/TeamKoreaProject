package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.EmailVerificationCode;

import java.util.Optional;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {

    Optional<EmailVerificationCode> findTopByEmailAndPurposeOrderByCreatedAtDesc(
            String email,
            String purpose);

    void deleteAllByEmailAndPurpose(String email, String purpose);
}