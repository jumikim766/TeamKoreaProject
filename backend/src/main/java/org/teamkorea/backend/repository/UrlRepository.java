package org.teamkorea.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.Url;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    // 중복 방지 핵심
    Optional<Url> findByUrlHash(String urlHash);

    boolean existsByUrlHash(String urlHash);

    Page<Url> findByDomainContainingIgnoreCase(String domain, Pageable pageable);
}