package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.Notification;
import org.teamkorea.backend.domain.User;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 사용자의 읽지 않은 알림을 최신순으로 조회합니다.
     */
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
}