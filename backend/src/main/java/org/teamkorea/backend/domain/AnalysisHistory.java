package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_history")
@Getter
@Setter // 필요시 수정 가능하도록 추가
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AnalysisHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 사용자 외래키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private UrlAnalysis urlAnalysis; // 분석 결과 외래키 (이름 통일)

    @Column(name = "source", nullable = false, length = 20)
    private String source; // 출처: EMAIL, WEB, MANUAL 등

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 분석 히스토리 생성을 위한 정적 팩토리 메서드
     */
    public static AnalysisHistory createHistory(User user, UrlAnalysis analysis, String source) {
        return AnalysisHistory.builder()
                .user(user)
                .urlAnalysis(analysis)
                .source(source)
                .build();
    }
}