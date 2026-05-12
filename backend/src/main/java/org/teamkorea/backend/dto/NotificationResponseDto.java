package org.teamkorea.backend.dto;

import lombok.*;
import org.teamkorea.backend.domain.Notification;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {
    private Long notificationId;
    private Long analysisId;
    private String title;
    private String message;
    private Boolean isRead;
    private String createdAt;

    public static NotificationResponseDto from(Notification noti) {
        return NotificationResponseDto.builder()
                .notificationId(noti.getNotificationId())
                .analysisId(noti.getUrlAnalysis().getAnalysisId())
                .title(noti.getTitle())
                .message(noti.getMessage())
                .isRead(noti.getIsRead())
                .createdAt(noti.getCreatedAt().toString())
                .build();
    }
}