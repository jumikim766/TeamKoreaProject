package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.Reports;

public interface ReportsRepository extends JpaRepository<Reports, Long> {
}