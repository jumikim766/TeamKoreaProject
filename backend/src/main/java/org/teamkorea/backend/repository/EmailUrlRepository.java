package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.Email;
import org.teamkorea.backend.domain.EmailUrl;

import java.util.List;

public interface EmailUrlRepository extends JpaRepository<EmailUrl, Long> {

    // 특정 이메일의 URL 목록 조회
    List<EmailUrl> findAllByEmail(Email email);
}