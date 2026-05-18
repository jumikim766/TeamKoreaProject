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

    @PostMapping
    public ResponseEntity<BaseResponse<ReportResponseDto>> createReport(
            @RequestBody ReportRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        // 컨트롤러에서는 String(이메일)을 서비스로 넘깁니다.
        ReportResponseDto response = reportService.createReport(request, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("신고가 접수되었습니다.", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<Map<String, List<ReportResponseDto>>>> getMyReports(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        // 여기서도 String(이메일)을 서비스로 넘깁니다.
        List<ReportResponseDto> reports = reportService.getMyReports(userDetails.getUsername());

        Map<String, List<ReportResponseDto>> data = new HashMap<>();
        data.put("reports", reports);

        return ResponseEntity.ok(
                BaseResponse.success("신고 내역 조회에 성공했습니다.", data)
        );
    }
}