package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.domain.UrlAnalysis;
import org.teamkorea.backend.service.AnalysisService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/url-analysis")
@RequiredArgsConstructor
public class UrlAnalysisController {

    private final AnalysisService analysisService;

    /**
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
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAnalyses(
            @RequestParam(required = false) String riskLevel
    ) {
        List<UrlAnalysis> analyses;

        if (riskLevel != null && !riskLevel.isBlank()) {
            analyses = analysisService.getAnalysesByRiskLevel(riskLevel);
        } else {
            analyses = analysisService.getAllAnalyses();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "분석 결과 목록 조회에 성공했습니다.");
        response.put("data", Map.of("analyses", analyses));

        return ResponseEntity.ok(response);
    }

    /**
     * 분석 결과 상세 조회 (GET)
     */
    @GetMapping("/{analysisId}")
    public ResponseEntity<Map<String, Object>> getAnalysisById(@PathVariable Long analysisId) {
        try {
            UrlAnalysis analysis = analysisService.getAnalysisById(analysisId);

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

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "분석 결과 상세 조회에 성공했습니다.");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("data", null);

            return ResponseEntity.status(404).body(response);
        }
    }
}