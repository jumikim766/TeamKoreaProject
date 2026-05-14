package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.*;
import org.teamkorea.backend.dto.*;
import org.teamkorea.backend.repository.*;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportsRepository reportsRepository;
    private final UserRepository userRepository;
    private final UrlRepository urlRepository;

    public ReportResponseDto createReport(String email, ReportRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("신고자를 찾을 수 없습니다."));

        Url url = null;
        String finalReportedUrl = request.getReportedUrl();

        if (request.getUrlId() != null) {
            url = urlRepository.findById(request.getUrlId())
                    .orElseThrow(() -> new IllegalArgumentException("신고할 URL 정보를 찾을 수 없습니다."));
            // [수정] DB에 저장된 정규화된 URL이 있다면 우선 사용
            finalReportedUrl = url.getNormalizedUrl(); 
        }

        // [수정] 명세서 7, 8페이지 status 초기값 'RECEIVED' 반영 
        Reports report = Reports.builder()
                .user(user)
                .url(url)
                .reportedUrl(finalReportedUrl)
                .reason(request.getReason())
                .status("RECEIVED") 
                .build();

        Reports savedReport = reportsRepository.save(report);

        return ReportResponseDto.builder()
                .reportId(savedReport.getReportId())
                .status(savedReport.getStatus())
                .createdAt(savedReport.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ReportResponseDto> getReportsByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return reportsRepository.findByUser_UserId(user.getUserId())
                .stream()
                .map(ReportResponseDto::new)
                .toList();
    }
}