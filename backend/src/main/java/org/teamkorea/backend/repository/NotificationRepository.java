package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.Notification;
import org.teamkorea.backend.domain.User;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 사용자별 읽지 않은 알림 최신순 조회 
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
}