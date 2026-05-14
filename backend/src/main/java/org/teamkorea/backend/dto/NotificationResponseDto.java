package org.teamkorea.backend.dto;

import lombok.*;
import org.teamkorea.backend.domain.Notification;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDto {
    private Long notificationId;
    private Long analysisId;
    private String channel;
    private String title;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;

    /**
     * [이 부분이 없어서 오류가 나는 것입니다]
     * Notification 엔티티를 DTO로 변환하는 정적 메서드
     */
    public static NotificationResponseDto from(Notification notification) {
        return NotificationResponseDto.builder()
                .notificationId(notification.getNotificationId())
                .analysisId(notification.getUrlAnalysis() != null ? 
                             notification.getUrlAnalysis().getAnalysisId() : null)
                .channel(notification.getChannel())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}