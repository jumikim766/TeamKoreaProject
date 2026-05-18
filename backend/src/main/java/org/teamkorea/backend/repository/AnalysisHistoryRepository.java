package org.teamkorea.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.AnalysisHistory;
import org.teamkorea.backend.domain.User;

public interface AnalysisHistoryRepository extends JpaRepository<AnalysisHistory, Long> {
    // 기존 메서드 유지
    // List<AnalysisHistory> findByUser_UserId(Long userId);

    // [추가] 서비스 로직 페이징 대응 
    Page<AnalysisHistory> findByUser(User user, Pageable pageable);
}