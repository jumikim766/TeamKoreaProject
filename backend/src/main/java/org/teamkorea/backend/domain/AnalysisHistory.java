package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_history")
@Getter
@Setter // 서비스단에서 필드 수정이 필요할 경우를 대비해 추가
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
    private User user; // analysis_history.user_id -> users.user_id [cite: 241, 248]

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private UrlAnalysis urlAnalysis; // 필드명을 urlAnalysis로 변경하여 명확화 [cite: 137, 248]

    @Column(name = "source", nullable = false, length = 20)
    private String source; // 출처: EMAIL, WEB, MANUAL 등 [cite: 126, 248]

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 

    /**
     * 분석 히스토리 생성을 위한 정적 팩토리 메서드
     * 서비스 레이어에서 AnalysisHistory.createHistory(...) 형태로 깔끔하게 사용 가능합니다.
     */
    public static AnalysisHistory createHistory(User user, UrlAnalysis analysis, String source) {
        return AnalysisHistory.builder()
                .user(user)
                .urlAnalysis(analysis)
                .source(source)
                .build();
    }
}