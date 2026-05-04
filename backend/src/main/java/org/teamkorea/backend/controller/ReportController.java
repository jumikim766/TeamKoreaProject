package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.dto.BaseResponse;
import org.teamkorea.backend.dto.ReportRequestDto;
import org.teamkorea.backend.dto.ReportResponseDto;
import org.teamkorea.backend.service.ReportService;

import java.security.Principal;
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
            Principal principal
    ) {
        // JWT 인증 후에는 principal에서 현재 사용자 이메일을 가져옴
        String email = getLoginEmail(principal);

        ReportResponseDto response = reportService.createReport(email, request);

        // API 명세서 기준: 201 Created + "신고가 접수되었습니다."
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("신고가 접수되었습니다.", response));
    }

    /**
     * 내 신고 내역 조회
     */
    @GetMapping
    public ResponseEntity<BaseResponse<Map<String, List<ReportResponseDto>>>> getMyReports(
            Principal principal
    ) {
        // JWT 인증 후에는 principal에서 현재 사용자 이메일을 가져옴
        String email = getLoginEmail(principal);

        List<ReportResponseDto> reports = reportService.getReportsByEmail(email);

        // API 명세서 기준: data 안에 reports 배열로 감싸서 반환
        Map<String, List<ReportResponseDto>> data = new HashMap<>();
        data.put("reports", reports);

        return ResponseEntity.ok(
                BaseResponse.success("신고 내역 조회에 성공했습니다.", data)
        );
    }

    private String getLoginEmail(Principal principal) {
        if (principal == null) {
            // 임시 테스트용 이메일을 쓰는 것보다 명확하게 예외 처리하는 편이 안전함
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        return principal.getName();
    }
}