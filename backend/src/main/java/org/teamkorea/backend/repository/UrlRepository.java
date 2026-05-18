package org.teamkorea.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.teamkorea.backend.domain.Url;
import org.teamkorea.backend.domain.RiskLevel;

import java.util.List;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    // 중복 방지 핵심
    Optional<Url> findByUrlHash(String urlHash);

    boolean existsByUrlHash(String urlHash);

    Page<Url> findByDomainContainingIgnoreCase(String domain, Pageable pageable);

    // URL 목록 조회
    @Query("""
            SELECT u
            FROM Url u
            WHERE (:domain IS NULL OR LOWER(u.domain) LIKE LOWER(CONCAT('%', :domain, '%')))
                AND (
                    :riskLevel IS NULL
                    OR EXISTS (
                        SELECT 1
                        FROM UrlAnalysis ua
                        WHERE ua.url = u
                            AND ua.riskLevel = :riskLevel
                            AND ua.analyzedAt = (
                                SELECT MAX(ua2.analyzedAt)
                                FROM UrlAnalysis ua2
                                WHERE ua2.url = u
                            )
                    )
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
            @Param("riskLevel") RiskLevel riskLevel,
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

    // 🚨 핵심 수정 포인트: 기존의 Object 반환 메서드와 잘못된 findById 코드를 완전히 삭제했습니다.
    // 이 메서드가 정상적으로 Optional<Url>을 반환해야 서비스와 연동됩니다.
    Optional<Url> findByNormalizedUrl(String normalizedUrl);
}