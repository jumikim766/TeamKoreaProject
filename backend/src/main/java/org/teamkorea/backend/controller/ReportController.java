package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.config.UserDetailsImpl;
import org.teamkorea.backend.dto.BaseResponse;
import org.teamkorea.backend.dto.ReportRequestDto;
import org.teamkorea.backend.dto.ReportResponseDto;
import org.teamkorea.backend.service.ReportService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 사용자 신고 등록
     * 에러 해결: email 대신 User 객체를 직접 전달하도록 수정
     */
    @PostMapping
    public ResponseEntity<BaseResponse<ReportResponseDto>> createReport(
            @RequestBody ReportRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        // userDetails에서 직접 User 엔티티를 꺼내서 전달
        ReportResponseDto response = reportService.createReport(userDetails.getUser(), request);

        // API 명세서 기준: 201 Created + "신고가 접수되었습니다."
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("신고가 접수되었습니다.", response));
    }

    /**
     * 내 신고 내역 조회
     */
    @GetMapping
    public ResponseEntity<BaseResponse<Map<String, List<ReportResponseDto>>>> getMyReports(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        // 이메일 기반 조회 로직 유지
        List<ReportResponseDto> reports = reportService.getReportsByEmail(userDetails.getUsername());

        Map<String, List<ReportResponseDto>> data = new HashMap<>();
        data.put("reports", reports);

        return ResponseEntity.ok(
                BaseResponse.success("신고 내역 조회에 성공했습니다.", data)
        );
    }
}