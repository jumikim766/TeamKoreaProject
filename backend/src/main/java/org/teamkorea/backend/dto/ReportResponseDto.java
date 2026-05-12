package org.teamkorea.backend.dto;

import lombok.*;
import org.teamkorea.backend.domain.Reports;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDto {

    private Long reportId;      // 
    private Long urlId;         // DB 연관관계 기반 
    private String reportedUrl; // 
    private String reason;      // 
    private String status;      // "RECEIVED" 
    private String createdAt;   // ISO 8601 형식 [cite: 218]
    private String updatedAt;   // ISO 8601 형식 [cite: 218]

    /**
     * Report 엔티티 → DTO 변환 생성자
     * [수정 포인트] 클래스명을 Reports에서 Report로 변경하고, 날짜 형식을 명세서 규격에 맞췄습니다.
     */
    public ReportResponseDto(Reports report) {
        this.reportId = report.getReportId();
        
        // URL이 연결된 경우에만 ID 추출 (null-safe) 
        this.urlId = (report.getUrl() != null) ? report.getUrl().getUrlId() : null;
        
        this.reportedUrl = report.getReportedUrl(); // 
        this.reason = report.getReason(); // 
        this.status = report.getStatus(); // 
        
        // LocalDateTime을 명세서 규격 String으로 변환 [cite: 218]
        this.createdAt = (report.getCreatedAt() != null) 
                ? report.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
        this.updatedAt = (report.getUpdatedAt() != null) 
                ? report.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }
}