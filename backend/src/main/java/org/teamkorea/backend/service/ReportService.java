package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.*;
import org.teamkorea.backend.dto.*;
import org.teamkorea.backend.repository.ReportsRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportsRepository reportRepository;

   // ReportService.java 내의 createReport 메서드 수정
@Transactional
public ReportResponseDto createReport(User email, ReportRequestDto request) { // [수정] 파라미터를 Long userId 대신 User user로 변경
    
    // 1. Report 엔티티 생성 (DB 최종 코드 필드명 일치)
    Reports report = Reports.builder()
            .user(email)
            .reportedUrl(request.getReportedUrl()) 
            .reason(request.getReason())
            .status("RECEIVED") 
            .build();

    Reports savedReport = reportRepository.save(report);

    // 2. DTO 반환 (생성자 대신 Builder 사용으로 에러 방지)
    return ReportResponseDto.builder()
            .reportId(savedReport.getReportId())
            .status(savedReport.getStatus())
            .createdAt(savedReport.getCreatedAt().toString()) 
            .build();
}

public List<ReportResponseDto> getReportsByEmail(String email) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getReportsByEmail'");
}
}