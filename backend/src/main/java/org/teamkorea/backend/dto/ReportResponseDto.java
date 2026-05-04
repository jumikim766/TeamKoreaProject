package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.teamkorea.backend.domain.Reports;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDto {

    private Long reportId;

    // 분석 결과와 연결된 URL ID (없을 수도 있음)
    private Long urlId;

    private String reportedUrl;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Reports 엔티티 → DTO 변환
     */
    public ReportResponseDto(Reports report) {
        this.reportId = report.getReportId();

        // Lazy 로딩 방어 + null-safe 처리
        this.urlId = (report.getUrl() != null)
                ? report.getUrl().getUrlId()
                : null;

        this.reportedUrl = report.getReportedUrl();
        this.reason = report.getReason();
        this.status = report.getStatus();
        this.createdAt = report.getCreatedAt();
        this.updatedAt = report.getUpdatedAt();
    }
}