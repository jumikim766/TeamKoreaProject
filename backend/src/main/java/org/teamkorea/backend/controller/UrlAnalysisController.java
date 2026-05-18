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
import org.teamkorea.backend.domain.UrlAnalysis;
import org.teamkorea.backend.dto.*;
import org.teamkorea.backend.service.AnalysisService;
import org.teamkorea.backend.service.NotificationService;

@RestController
@RequestMapping("/api/url-analysis")
@RequiredArgsConstructor
public class UrlAnalysisController {

    private final AnalysisService analysisService;
    private final NotificationService notificationService; // 알림 서비스 주입

    /**
     * 1. URL 분석 실행
     */
    @PostMapping("/analyze")
    public ResponseEntity<BaseResponse<UrlAnalysis>> analyzeUrl(
            @RequestParam Long userId,
            @RequestParam Long urlId
    ) {
        UrlAnalysis result = analysisService.analyzeAndSave(userId, urlId);
        return ResponseEntity.ok(BaseResponse.success("URL 분석이 완료되었습니다.", result));
    }

    /**
     * 2. 내 분석 히스토리 목록 조회
     */
    @GetMapping("/history")
    public ResponseEntity<BaseResponse<Page<AnalysisHistoryResponseDto>>> getAnalysisList(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AnalysisHistoryResponseDto> response = analysisService.getAnalysisList(userDetails.getUser(), pageable);
        return ResponseEntity.ok(BaseResponse.success("분석 결과 목록 조회에 성공했습니다.", response));
    }

    /**
     * 3. 분석 결과 상세 조회
     */
    @GetMapping("/{analysisId}")
    public ResponseEntity<BaseResponse<AnalysisDetailResponseDto>> getAnalysisDetail(
            @PathVariable Long analysisId
    ) {
        AnalysisDetailResponseDto detail = analysisService.getDetail(analysisId);
        return ResponseEntity.ok(BaseResponse.success("분석 결과 상세 조회에 성공했습니다.", detail));
    }

    /**
     * 4. 알림 목록 조회 (페이징 + 안읽은 알림 필터 조건 보완)
     * API 명세: GET /api/url-analysis/notifications?unreadOnly=true
     */
    @GetMapping("/notifications")
    public ResponseEntity<BaseResponse<Page<NotificationResponseDto>>> getNotifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<NotificationResponseDto> response = 
                notificationService.getNotifications(userDetails.getUser(), unreadOnly, pageable);
        return ResponseEntity.ok(BaseResponse.success("알림 목록 조회에 성공했습니다.", response));
    }

    /**
     * 5. 알림 읽음 단건 처리 (신규 완료 단계 추가 - FNC-063)
     * API 명세: PATCH /api/url-analysis/notifications/{notificationId}/read
     */
    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<BaseResponse<NotificationResponseDto>> markNotificationAsRead(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long notificationId
    ) {
        NotificationResponseDto response = notificationService.markAsRead(notificationId, userDetails.getUser());
        return ResponseEntity.ok(BaseResponse.success("알림 읽음 처리가 완료되었습니다.", response));
    }
}