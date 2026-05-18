package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.teamkorea.backend.domain.Report;
import org.teamkorea.backend.domain.User;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // 1. 특정 사용자의 신고 내역 전체 조회 (ReportService에서 사용)
    List<Report> findByUser(User user);

    // 2. 기존 호환성 및 추후 관리자(Admin) 페이지 확장을 위해 유지하는 메서드들
    List<Report> findByUser_UserId(Long userId);

    List<Report> findByStatus(String status);

    List<Report> findByUrl_UrlId(Long urlId);
}