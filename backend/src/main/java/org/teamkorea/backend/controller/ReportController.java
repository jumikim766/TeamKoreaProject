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
     */
    @PostMapping
    public ResponseEntity<BaseResponse<ReportResponseDto>> createReport(
            @RequestBody ReportRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        // [수정 포인트 1] 메서드 명을 createReports -> createReport로 변경 (s 제거)
        // [수정 포인트 2] 서비스를 String email을 받도록 고쳤으므로 userDetails.getUsername() 전달
        ReportResponseDto response = reportService.createReport(userDetails.getUsername(), request);

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
        List<ReportResponseDto> reports = reportService.getReportsByEmail(userDetails.getUsername());

        Map<String, List<ReportResponseDto>> data = new HashMap<>();
        data.put("reports", reports);

        return ResponseEntity.ok(
                BaseResponse.success("신고 내역 조회에 성공했습니다.", data)
        );
    }
}