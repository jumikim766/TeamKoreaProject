package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter // 관리자 기능 등에서 상태(status) 변경 처리를 위해 유지
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "reports")
public class Report { 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // reports.user_id → users.user_id FK 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id")
    private Url url; // 분석된 적이 없는 URL일 수 있으므로 NULL 허용 

    @Column(name = "reported_url", nullable = false, columnDefinition = "TEXT")
    private String reportedUrl; 

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason; 

    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private String status = "RECEIVED"; // RECEIVED, REVIEWING, COMPLETED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 기존 서비스 코드 호환성 유지용 커스텀 생성자
    public Report(User user, Url url, String reportedUrl, String reason) {
        this.user = user;
        this.url = url;
        this.reportedUrl = reportedUrl;
        this.reason = reason;
        this.status = "RECEIVED";
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = "RECEIVED";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}