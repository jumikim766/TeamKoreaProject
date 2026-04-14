package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.EmailUrl;

public interface EmailUrlRepository extends JpaRepository<EmailUrl, Long> {
}