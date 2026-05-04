package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.Reports;
import org.teamkorea.backend.domain.Url;
import org.teamkorea.backend.domain.User;
import org.teamkorea.backend.dto.ReportRequestDto;
import org.teamkorea.backend.dto.ReportResponseDto;
import org.teamkorea.backend.repository.ReportsRepository;
import org.teamkorea.backend.repository.UrlRepository;
import org.teamkorea.backend.repository.UserRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportsRepository reportsRepository;
    private final UserRepository userRepository;
    private final UrlRepository urlRepository;

    /**
     * 사용자 신고 등록
     */
    public ReportResponseDto createReport(String email, ReportRequestDto request) {
        // 신고자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("신고자를 찾을 수 없습니다."));

        // urlId는 선택값이므로 null이면 연결하지 않음
        Url url = null;
        if (request.getUrlId() != null) {
            url = urlRepository.findById(request.getUrlId())
                    .orElseThrow(() -> new IllegalArgumentException("신고할 URL 정보를 찾을 수 없습니다."));
        }

        Reports report = Reports.builder()
                .user(user)
                .url(url)
                .reportedUrl(request.getReportedUrl())
                .reason(request.getReason())
                .status("RECEIVED")
                .build();

        Reports savedReport = reportsRepository.save(report);

        // API 명세서 POST 응답: reportId, status, createdAt 중심
        return ReportResponseDto.builder()
                .reportId(savedReport.getReportId())
                .status(savedReport.getStatus())
                .createdAt(savedReport.getCreatedAt())
                .build();
    }

    /**
     * 내 신고 내역 조회
     */
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