package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "analysis_history")
public class AnalysisHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_analysis_id")
    private UrlAnalysis urlAnalysis;

    private String userEmail;

    private LocalDateTime viewedAt;

    public AnalysisHistory(UrlAnalysis urlAnalysis, String userEmail) {
        this.urlAnalysis = urlAnalysis;
        this.userEmail = userEmail;
        this.viewedAt = LocalDateTime.now();
    }
}
