package org.teamkorea.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.Url;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    // 중복 방지 핵심
    Optional<Url> findByUrlHash(String urlHash);

    boolean existsByUrlHash(String urlHash);

    Page<Url> findByDomainContainingIgnoreCase(String domain, Pageable pageable);

    // URL 목록 조회 - DB 조회 단계에서 처리
    @Query("""
                    SELECT u
                    FROM Url u
                    WHERE (:domain IS NULL OR LOWER(u.domain) LIKE LOWER(CONCAT('%', :domain, '%')))
                        AND (
                            :riskLevel IS NULL
                            OR (
                                SELECT ua.riskLevel
                                FROM UrlAnalysis ua
                                WHERE ua.url = u
                                    AND ua.analyzedAt = (
                                        SELECT MAX(ua2.analyzedAt)
                                        FROM UrlAnalysis ua2
                                        WHERE ua2.url = u)
                                ) = :riskLevel
                        )
                        AND (
                            :isAnalyzed IS NULL
                            OR (:isAnalyzed = true AND EXISTS (
                                SELECT 1
                                FROM UrlAnalysis ua4
                                WHERE ua4.url = u
                            ))
                            OR (:isAnalyzed = false AND NOT EXISTS (
                                SELECT 1
                                FROM UrlAnalysis ua5
                                WHERE ua5.url = u
                            ))
                        )
            """)
    Page<Url> searchUrls(
            @Param("domain") String domain,
            @Param("riskLevel") org.teamkorea.backend.domain.RiskLevel riskLevel,
            @Param("isAnalyzed") Boolean isAnalyzed,
            Pageable pageable);

    // 전체 URL 통계 조회용
    @Query("""
            SELECT u
            FROM Url u
            WHERE (:domain IS NULL OR LOWER(u.domain) LIKE LOWER(CONCAT('%', :domain, '%')))
              AND (
                    :isAnalyzed IS NULL
                    OR (:isAnalyzed = true AND EXISTS (
                        SELECT 1
                        FROM UrlAnalysis ua1
                        WHERE ua1.url = u
                    ))
                    OR (:isAnalyzed = false AND NOT EXISTS (
                        SELECT 1
                        FROM UrlAnalysis ua2
                        WHERE ua2.url = u
                    ))
              )
            """)
    List<Url> findUrlsForStatistics(
            @Param("domain") String domain,
            @Param("isAnalyzed") Boolean isAnalyzed);
}