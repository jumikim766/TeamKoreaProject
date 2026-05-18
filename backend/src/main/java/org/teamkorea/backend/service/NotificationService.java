package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.Notification;
import org.teamkorea.backend.domain.User;
import org.teamkorea.backend.dto.NotificationResponseDto;
import org.teamkorea.backend.repository.NotificationRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 사용자 알림 목록 조회 (페이징 적용)
     * @param onlyUnread true일 경우 읽지 않은 알림만 조회
     */
    public Page<NotificationResponseDto> getNotifications(User user, boolean onlyUnread, Pageable pageable) {
        Page<Notification> notifications;
        
        if (onlyUnread) {
            notifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user, pageable);
        } else {
            notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        }

        return notifications.map(this::convertToDto);
    }

    /**
     * 알림 단건 읽음 완료 처리 (FNC-063)
     */
    @Transactional
    public NotificationResponseDto markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findByNotificationIdAndUser(notificationId, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 알림을 찾을 수 없거나 접근 권한이 없습니다."));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now()); // 최종 DB 명세의 read_at 매핑
        }

        return convertToDto(notification);
    }

    // Entity -> DTO 변환 헬퍼 메서드
    private NotificationResponseDto convertToDto(Notification notification) {
        return NotificationResponseDto.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUser().getUserId())
                .analysisId(notification.getAnalysisId()) // URLAnalysis 연관관계 ID
                .channel(notification.getChannel())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}