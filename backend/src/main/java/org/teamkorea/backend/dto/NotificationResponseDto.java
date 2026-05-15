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
    private String createdAt; // API 응답 일관성을 위해 String으로 유지 (toString 사용)

    /**
     * Notification 엔티티를 DTO로 변환하는 정적 메서드
     */
    public static NotificationResponseDto from(Notification notification) {
        return NotificationResponseDto.builder()
                .notificationId(notification.getNotificationId())
                // 분석 결과(UrlAnalysis)가 null일 경우를 대비한 안전한 처리
                .analysisId(notification.getUrlAnalysis() != null ? 
                             notification.getUrlAnalysis().getAnalysisId() : null)
                .channel(notification.getChannel())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt() != null ? 
                           notification.getCreatedAt().toString() : null)
                .build();
    }
}