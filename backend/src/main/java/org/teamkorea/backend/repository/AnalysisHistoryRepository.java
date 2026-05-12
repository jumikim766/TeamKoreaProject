package org.teamkorea.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.teamkorea.backend.domain.AnalysisHistory;
import org.teamkorea.backend.domain.User;

public interface AnalysisHistoryRepository extends JpaRepository<AnalysisHistory, Long> {
    // 페치 조인을 통해 N+1 문제를 방지하며 히스토리 조회 [cite: 142-146]
    @Query("SELECT h FROM AnalysisHistory h JOIN FETCH h.urlAnalysis a JOIN FETCH a.url WHERE h.user = :user")
    Page<AnalysisHistory> findByUser(@Param("user") User user, Pageable pageable);
}