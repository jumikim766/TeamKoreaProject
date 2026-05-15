package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.teamkorea.backend.domain.Email;
import org.teamkorea.backend.domain.EmailUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.teamkorea.backend.domain.RiskLevel;

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

    // 나의 URL 목록 조회
    @Query("""
            SELECT eu
            FROM EmailUrl eu
            JOIN FETCH eu.email e
            JOIN FETCH e.account a
            JOIN FETCH eu.url u
            WHERE a.user.userId = :userId
              AND (:accountId IS NULL OR a.accountId = :accountId)
              AND (:domain IS NULL OR LOWER(u.domain) LIKE LOWER(CONCAT('%', :domain, '%')))
              AND (
                    :riskLevel IS NULL
                    OR (
                        SELECT ua.riskLevel
                        FROM UrlAnalysis ua
                        WHERE ua.url = u
                          AND ua.analyzedAt = (
                                SELECT MAX(ua2.analyzedAt)
                                FROM UrlAnalysis ua2
                                WHERE ua2.url = u
                          )
                    ) = :riskLevel
              )
              AND (
                    :isAnalyzed IS NULL
                    OR (:isAnalyzed = true AND EXISTS (
                        SELECT 1
                        FROM UrlAnalysis ua3
                        WHERE ua3.url = u
                    ))
                    OR (:isAnalyzed = false AND NOT EXISTS (
                        SELECT 1
                        FROM UrlAnalysis ua4
                        WHERE ua4.url = u
                    ))
              )
            """)
    Page<EmailUrl> searchMyUrls(
            @Param("userId") Long userId,
            @Param("accountId") Long accountId,
            @Param("domain") String domain,
            @Param("riskLevel") RiskLevel riskLevel,
            @Param("isAnalyzed") Boolean isAnalyzed,
            Pageable pageable);

    // 나의 URL 통계 조회용
    @Query("""
            SELECT eu
            FROM EmailUrl eu
            JOIN FETCH eu.email e
            JOIN FETCH e.account a
            JOIN FETCH eu.url u
            WHERE a.user.userId = :userId
              AND (:accountId IS NULL OR a.accountId = :accountId)
              AND (:domain IS NULL OR LOWER(u.domain) LIKE LOWER(CONCAT('%', :domain, '%')))
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
    List<EmailUrl> findMyUrlsForStatistics(
            @Param("userId") Long userId,
            @Param("accountId") Long accountId,
            @Param("domain") String domain,
            @Param("isAnalyzed") Boolean isAnalyzed);
}