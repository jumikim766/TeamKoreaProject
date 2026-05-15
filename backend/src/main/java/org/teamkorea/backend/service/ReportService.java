package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.*;
import org.teamkorea.backend.dto.*;
import org.teamkorea.backend.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportsRepository reportRepository;
    private final UserRepository userRepository;
    private final UrlRepository urlRepository;

    /**
     * 새로운 신고를 접수합니다.
     */
    public ReportResponseDto createReport(String email, ReportRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("신고자를 찾을 수 없습니다."));

        Url url = null;
        String finalReportedUrl = request.getReportedUrl();

        // 만약 특정 분석 결과나 URL ID가 연관되어 있다면 정보를 가져옴
        if (request.getUrlId() != null) {
            url = urlRepository.findById(request.getUrlId())
                    .orElseThrow(() -> new IllegalArgumentException("신고할 URL 정보를 찾을 수 없습니다."));
            finalReportedUrl = url.getNormalizedUrl(); 
        }

        // 1. Reports 엔티티 생성 (데이터 사전의 status: RECEIVED 반영)
        Reports report = Reports.builder()
                .user(user)
                .url(url)
                .reportedUrl(finalReportedUrl)
                .reason(request.getReason())
                .status("RECEIVED") 
                .build();

        Reports savedReport = reportRepository.save(report);

        // 2. DTO 반환 (createdAt은 String으로 변환하여 전달)
        return ReportResponseDto.builder()
                .reportId(savedReport.getReportId())
                .status(savedReport.getStatus())
                .createdAt(savedReport.getCreatedAt() != null ? 
                           savedReport.getCreatedAt().toString() : null)
                .build();
    }

    /**
     * 사용자의 이메일을 기반으로 신고 내역 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<ReportResponseDto> getReportsByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 리포지토리의 findByUser_UserId 또는 findByUser 메서드 사용
        return reportRepository.findByUser(user)
                .stream()
                .map(report -> ReportResponseDto.builder()
                        .reportId(report.getReportId())
                        .status(report.getStatus())
                        .createdAt(report.getCreatedAt().toString())
                        .build())
                .collect(Collectors.toList());
    }
}