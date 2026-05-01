package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.Reports;

import java.util.List;

public interface ReportsRepository extends JpaRepository<Reports, Long> {

    List<Reports> findByUser_UserId(Long userId);

    List<Reports> findByStatus(String status);

    List<Reports> findByUrl_UrlId(Long urlId);
}