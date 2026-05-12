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

@RestController
@RequestMapping("/api/url-analysis")
@RequiredArgsConstructor
public class UrlAnalysisController {

    private final AnalysisService analysisService;

    /**
     * URL 분석 실행
     * 에러 해결: service.analyzeAndSave(userId, urlId) 호출 형식을 서비스와 일치시킴
     */
    @PostMapping("/analyze")
    public ResponseEntity<BaseResponse<UrlAnalysis>> analyzeUrl(
            @RequestParam Long userId,
            @RequestParam Long urlId
    ) {
        UrlAnalysis result = analysisService.analyzeAndSave(userId, urlId);
        
        return ResponseEntity.ok(
                BaseResponse.success("URL 분석이 완료되었습니다.", result)
        );
    }

    /**
     * 분석 결과 목록 조회 (페이징 + 필터)
     * 에러 해결 1: AnalysisListPageResponseDto 대신 Page<AnalysisHistoryResponseDto> 사용
     * 에러 해결 2: 서비스의 getAnalysisList(User, Pageable) 형식에 맞게 호출
     */
    @GetMapping("/history")
    public ResponseEntity<BaseResponse<Page<AnalysisHistoryResponseDto>>> getAnalysisList(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AnalysisHistoryResponseDto> response =
                analysisService.getAnalysisList(userDetails.getUser(), pageable);

        return ResponseEntity.ok(
                BaseResponse.success("분석 결과 목록 조회에 성공했습니다.", response)
        );
    }

    /**
     * 분석 결과 상세 조회
     */
    @GetMapping("/{analysisId}")
    public ResponseEntity<BaseResponse<AnalysisDetailResponseDto>> getAnalysisDetail(
            @PathVariable Long analysisId
    ) {
        AnalysisDetailResponseDto detail = analysisService.getDetail(analysisId);

        return ResponseEntity.ok(
                BaseResponse.success("분석 결과 상세 조회에 성공했습니다.", detail)
        );
    }
}