package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.Reports;
import org.teamkorea.backend.domain.User;

import java.util.List;

public interface ReportsRepository extends JpaRepository<Reports, Long> {

    // [해결 포인트] Service에서 호출하는 메서드 추가
    List<Reports> findByUser(User user);

    // 기존에 있던 메서드들도 호환성을 위해 유지
    List<Reports> findByUser_UserId(Long userId);

    List<Reports> findByStatus(String status);

    List<Reports> findByUrl_UrlId(Long urlId);
    
    // [참고] Optional<User> findById(User user)는 JpaRepository 기본 기능과 충돌하고 
    // 논리적으로 맞지 않아 삭제하거나 이름을 바꿔야 합니다.
}