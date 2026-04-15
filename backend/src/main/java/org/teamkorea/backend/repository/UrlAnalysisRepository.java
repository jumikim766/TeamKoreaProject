package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.UrlAnalysis;
import java.util.Optional;

public interface UrlAnalysisRepository extends JpaRepository<UrlAnalysis, Long> {
    Optional<UrlAnalysis> findByOriginalUrl(String originalUrl);
}

