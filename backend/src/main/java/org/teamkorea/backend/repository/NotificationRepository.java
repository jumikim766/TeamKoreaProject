package org.teamkorea.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.teamkorea.backend.domain.Notification;
import org.teamkorea.backend.domain.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 1. 특정 사용자의 전체 알림 페이징 조회
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 2. 특정 사용자의 안 읽은 알림 페이징 조회 (알림함 용도)
    Page<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    // 3. 특정 사용자의 안 읽은 알림 전체 리스트 (AnalysisService 내부 사용 용도)
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    // 4. 알림 단건 조회 (본인 확인용)
    Optional<Notification> findByNotificationIdAndUser(Long notificationId, User user);

    // 5. [추가] 특정 사용자의 안 읽은 알림 개수 조회 (프론트엔드 상단 배지 노출용)
    long countByUserAndIsReadFalse(User user);
}