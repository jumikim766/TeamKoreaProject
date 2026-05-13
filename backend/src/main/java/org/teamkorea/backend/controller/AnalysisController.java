package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.teamkorea.backend.config.UserDetailsImpl; 
import org.teamkorea.backend.dto.*;
import org.teamkorea.backend.service.AnalysisService;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    // 1. 분석 상세 조회
    @GetMapping("/analysis-detail/{analysisId}")
    public ResponseEntity<AnalysisDetailResponseDto> getDetail(@PathVariable Long analysisId) {
        AnalysisDetailResponseDto response = analysisService.getDetail(analysisId);
        return ResponseEntity.ok(response);
    }

    // 2. 알림 목록 조회
    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponseDto>> getNotifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<NotificationResponseDto> response = analysisService.getNotifications(userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    // 3. 내 분석 히스토리 조회 (에러 발생 지점 수정)
    // AnalysisController.java 내 해당 메서드 수정
@GetMapping("/analysis-history")
public ResponseEntity<Page<AnalysisHistoryResponseDto>> getHistory(
        @AuthenticationPrincipal UserDetailsImpl userDetails, 
        Pageable pageable) {
    
    // 서비스 호출 결과 타입을 명시적으로 지정
    Page<AnalysisHistoryResponseDto> response = analysisService.getAnalysisList(userDetails.getUser(), pageable);
    return ResponseEntity.ok(response);
}
}