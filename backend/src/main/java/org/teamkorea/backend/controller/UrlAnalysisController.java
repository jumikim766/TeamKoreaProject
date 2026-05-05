package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.dto.*;
import org.teamkorea.backend.service.AnalysisService;
import org.teamkorea.backend.domain.RiskLevel;

@RestController
@RequestMapping("/api/url-analysis")
@RequiredArgsConstructor
public class UrlAnalysisController {

    private final AnalysisService analysisService;

    /**
<<<<<<< HEAD
     * URL 분석 실행 (POST)
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeUrl(
            @RequestParam Long userId,
            @RequestParam Long urlId
    ) {
        UrlAnalysis result = analysisService.analyzeAndSave(userId, urlId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "URL 분석이 완료되었습니다.");
        response.put("data", result);

        return ResponseEntity.ok(response);
    }

    /**
     * 분석 결과 목록 조회 (GET)
=======
     * 분석 결과 목록 조회 (페이징 + 필터)
>>>>>>> origin/backend-dev
     */
    @GetMapping
    public ResponseEntity<BaseResponse<AnalysisListPageResponseDto>> getAnalysisList(
            @RequestParam(required = false) RiskLevel riskLevel, // Enum 적용
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
<<<<<<< HEAD
     * 분석 결과 상세 조회 (GET)
=======
     * 분석 결과 상세 조회
>>>>>>> origin/backend-dev
     */
    @GetMapping("/{analysisId}")
    public ResponseEntity<BaseResponse<AnalysisDetailResponseDto>> getAnalysisDetail(
            @PathVariable Long analysisId
    ) {

<<<<<<< HEAD
            Map<String, Object> data = new HashMap<>();
            data.put("analysisId", analysis.getAnalysisId());
            data.put("urlId", analysis.getUrl().getUrlId());
            data.put("sourceType", analysis.getSourceType());
            data.put("riskLevel", analysis.getRiskLevel());
            data.put("riskType", analysis.getRiskType());
            data.put("score", analysis.getScore());
            data.put("reasonSummary", analysis.getReasonSummary());
            data.put("featuresJson", analysis.getFeaturesJson());
            data.put("ruleVersion", analysis.getRuleVersion());
            data.put("analyzedAt", analysis.getAnalyzedAt());
=======
        AnalysisDetailResponseDto detail =
                analysisService.getDetail(analysisId);
>>>>>>> origin/backend-dev

        return ResponseEntity.ok(
                BaseResponse.success("분석 결과 상세 조회에 성공했습니다.", detail)
        );
    }
}