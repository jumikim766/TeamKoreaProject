package org.teamkorea.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.RiskLevel;
import org.teamkorea.backend.domain.UrlAnalysis;

import java.util.List;
import java.util.Optional;



public interface UrlAnalysisRepository extends JpaRepository<UrlAnalysis, Long> {

    // 특정 URL에 대한 분석 결과 목록 조회
    List<UrlAnalysis> findByUrl_UrlId(Long urlId);

    // riskLevel enum 기준 + 페이징 조회
    Page<UrlAnalysis> findByRiskLevel(RiskLevel riskLevel, Pageable pageable);

    // 특정 URL의 가장 최근 분석 결과 조회
    Optional<UrlAnalysis> findTopByUrl_UrlIdOrderByAnalyzedAtDesc(Long urlId);

    
}