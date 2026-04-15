package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Reports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_analysis_id")
    private UrlAnalysis urlAnalysis;

    @Column(columnDefinition = "TEXT")
    private String detailedReason; // 왜 위험한지 상세 설명

    private String detectionEngine; // AI vs Rule-based
}
