package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.Report;
import org.teamkorea.backend.domain.Url;
import org.teamkorea.backend.domain.User;
import org.teamkorea.backend.dto.ReportRequestDto;
import org.teamkorea.backend.dto.ReportResponseDto;
import org.teamkorea.backend.repository.ReportRepository;
import org.teamkorea.backend.repository.UrlRepository;
import org.teamkorea.backend.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final UrlRepository urlRepository;
    private final UserRepository userRepository;

    public ReportResponseDto createReport(ReportRequestDto request, String email) {
        
        // 1. 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 🚨 에러 원인 제거: DTO에 있는 정확한 이름인 getUrl() 하나만 사용합니다! (urlId 체크 등 싹 삭제)
        String targetUrl = request.getUrl(); 
        
        // 2. 입력된 URL 텍스트로 DB에서 매핑 검색
        Url existingUrl = urlRepository.findByNormalizedUrl(targetUrl).orElse(null);

        // 3. Report 엔티티 생성
        Report report = Report.builder()
                .user(user)
                .url(existingUrl)
                .reportedUrl(targetUrl) // DB에는 입력받은 URL 그대로 저장
                .reason(request.getReason())
                .status("RECEIVED")
                .build();

        // 4. DB 저장
        Report savedReport = reportRepository.save(report);

        // 5. 응답 DTO 반환
        return ReportResponseDto.builder()
                .reportId(savedReport.getReportId())
                .status(savedReport.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ReportResponseDto> getMyReports(String email) {
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return reportRepository.findByUser(user)
                .stream()
                .map(report -> ReportResponseDto.builder()
                        .reportId(report.getReportId())
                        .status(report.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
}