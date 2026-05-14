package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter // 서비스 레이어에서 상태 변경을 위해 추가
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 알림 수신자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id")
    private UrlAnalysis urlAnalysis; // 연관된 분석 결과 (없을 수 있음)

    @Column(name = "channel", nullable = false, length = 20)
    private String channel; // "WEB", "PUSH" 등

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, length = 1000)
    private String message; 

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false; 

    @Column(name = "read_at")
    private LocalDateTime readAt; // 읽은 시각 (데이터 사전 반영)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; 

    /**
     * 알림 읽음 처리 메서드
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}