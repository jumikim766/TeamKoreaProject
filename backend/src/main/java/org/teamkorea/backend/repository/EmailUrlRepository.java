package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.teamkorea.backend.domain.Email;
import org.teamkorea.backend.domain.EmailUrl;

import java.util.List;

public interface EmailUrlRepository extends JpaRepository<EmailUrl, Long> {

    // (사용 안함) Entity 기반 조회
    List<EmailUrl> findAllByEmail(Email email);

    // 이메일 ID 기준 URL 목록 조회
    List<EmailUrl> findByEmail_EmailId(Long emailId);

    // URL 개수
    int countByEmail_EmailId(Long emailId);

    // URL 목록 조회 (url까지 fetch)
    @Query("SELECT eu FROM EmailUrl eu JOIN FETCH eu.url WHERE eu.email.emailId = :emailId")
    List<EmailUrl> findByEmailIdWithUrl(@Param("emailId") Long emailId);
}