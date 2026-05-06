package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 엔티티용 기본 생성자
@AllArgsConstructor
@Builder // ReportService에서 Reports.builder() 사용 가능
@Table(name = "reports")
public class Reports {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // reports.user_id → users.user_id FK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id")
    private Url url; // url_id는 명세서상 NULL 허용

    @Column(name = "reported_url", nullable = false, columnDefinition = "TEXT")
    private String reportedUrl; // 사용자가 신고한 URL

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason; // 신고 사유

    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private String status = "RECEIVED"; // 기본 신고 상태

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성 시각

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 수정 시각

    // 기존 서비스 코드 호환용 생성자
    public Reports(User user, Url url, String reportedUrl, String reason) {
        this.user = user;
        this.url = url;
        this.reportedUrl = reportedUrl;
        this.reason = reason;
        this.status = "RECEIVED";
    }

    @PrePersist
    protected void onCreate() {
        // 저장 직전 생성/수정 시간 자동 입력
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // status가 비어 있으면 기본값 RECEIVED 적용
        if (this.status == null) {
            this.status = "RECEIVED";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // 수정될 때마다 updatedAt 갱신
        this.updatedAt = LocalDateTime.now();
    }
}