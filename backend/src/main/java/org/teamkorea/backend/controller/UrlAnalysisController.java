package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.domain.UrlAnalysis;
import org.teamkorea.backend.domain.RiskLevel;
import org.teamkorea.backend.dto.*;
import org.teamkorea.backend.service.AnalysisService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/url-analysis")
@RequiredArgsConstructor
public class UrlAnalysisController {

    private final AnalysisService analysisService;

    /**
     * URL 분석 실행 (서윤님 로직 반영)
     */
    @PostMapping("/analyze")
    public ResponseEntity<BaseResponse<UrlAnalysis>> analyzeUrl(
            @RequestParam Long userId,
            @RequestParam Long urlId
    ) {
        UrlAnalysis result = analysisService.analyzeAndSave(userId, urlId);
        
        // 팀원들의 공통 응답 규격인 BaseResponse 사용
        return ResponseEntity.ok(
                BaseResponse.success("URL 분석이 완료되었습니다.", result)
        );
    }

    /**
     * 분석 결과 목록 조회 (페이징 + 필터)
     */
    @GetMapping
    public ResponseEntity<BaseResponse<AnalysisListPageResponseDto>> getAnalysisList(
            @RequestParam(required = false) RiskLevel riskLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Enum → String으로 변환해서 서비스로 전달
        String level = (riskLevel != null) ? riskLevel.name() : null;

        AnalysisListPageResponseDto response =
                analysisService.getAnalysisList(level, page, size);

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
        AnalysisDetailResponseDto detail =
                analysisService.getDetail(analysisId);

        return ResponseEntity.ok(
                BaseResponse.success("분석 결과 상세 조회에 성공했습니다.", detail)
        );
    }
}