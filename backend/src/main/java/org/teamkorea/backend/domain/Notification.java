package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
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
    private User user; // notifications.user_id -> users.user_id 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id")
    private UrlAnalysis urlAnalysis; // notifications.analysis_id -> url_analysis.analysis_id 

    @Column(name = "channel", nullable = false, length = 20)
    private String channel; // "WEB" [cite: 180, 248]

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, length = 1000)
    private String message; 
    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false; 

    @Column(name = "read_at")
    private LocalDateTime readAt; 

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