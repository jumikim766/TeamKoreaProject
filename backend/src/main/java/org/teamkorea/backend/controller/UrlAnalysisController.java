package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.domain.UrlAnalysis;
import org.teamkorea.backend.service.AnalysisService;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class UrlAnalysisController {
    private final AnalysisService analysisService;

    @PostMapping
    public UrlAnalysis analyze(@RequestParam String url, @RequestParam String email) {
        return analysisService.analyzeAndSave(url, email);
    }
}