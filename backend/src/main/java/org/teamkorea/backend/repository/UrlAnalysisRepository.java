package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.UrlAnalysis;

import java.util.List;
import java.util.Optional;

public interface UrlAnalysisRepository extends JpaRepository<UrlAnalysis, Long> {

    List<UrlAnalysis> findByUrl_UrlId(Long urlId);

    List<UrlAnalysis> findByRiskLevel(String riskLevel);

    Optional<UrlAnalysis> findTopByUrl_UrlIdOrderByAnalyzedAtDesc(Long urlId);
}