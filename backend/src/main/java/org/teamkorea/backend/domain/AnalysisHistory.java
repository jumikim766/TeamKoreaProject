package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_history")
@Getter
@Builder // 객체 생성 시 builder() 방식 사용 가능
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 보호
@AllArgsConstructor
public class AnalysisHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // analysis_history.user_id → users.user_id FK 매핑

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private UrlAnalysis analysis; // analysis_history.analysis_id → url_analysis.analysis_id FK 매핑

    @Column(name = "source", nullable = false, length = 20)
    private String source; // 분석 요청 출처: WEB / EMAIL / MANUAL 등

    @CreationTimestamp // INSERT 시 현재 시간이 자동 저장됨
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 기존 코드에서 사용하던 생성자도 유지하여 다른 서비스 코드와의 호환성 확보
    public AnalysisHistory(User user, UrlAnalysis analysis, String source) {
        this.user = user;
        this.analysis = analysis;
        this.source = source;
    }
}