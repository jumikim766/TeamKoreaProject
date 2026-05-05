package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.teamkorea.backend.domain.Email;
import org.teamkorea.backend.domain.EmailUrl;

import java.util.List;

public interface EmailUrlRepository extends JpaRepository<EmailUrl, Long> {

    List<EmailUrl> findAllByEmail(Email email);

    List<EmailUrl> findByEmail_EmailId(Long emailId);

    int countByEmail_EmailId(Long emailId);

    // URL 조회용 fetch join (핵심)
    @Query("SELECT eu FROM EmailUrl eu JOIN FETCH eu.url WHERE eu.email.emailId = :emailId")
    List<EmailUrl> findByEmailIdWithUrl(Long emailId);
}