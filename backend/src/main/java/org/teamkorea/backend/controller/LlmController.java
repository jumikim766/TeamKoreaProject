package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.ai.LlmAnalysisService;
import org.teamkorea.backend.ai.dto.LlmAnalysisResponse;
import org.teamkorea.backend.dto.BaseResponse;

@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LlmController {

    private final LlmAnalysisService llmAnalysisService;

    @GetMapping("/test")
public ResponseEntity<BaseResponse<LlmAnalysisResponse>> testLlm(
        @RequestParam String url
) {
    LlmAnalysisResponse response = llmAnalysisService.analyze(
            url,
            extractDomain(url),
            "WARNING",
            60.0,
            false,
            false
    );

    return ResponseEntity.ok(
            BaseResponse.success("LLM 테스트가 완료되었습니다.", response)
    );
}

private String extractDomain(String url) {
    try {
        return java.net.URI.create(url).getHost();
    } catch (Exception e) {
        return "unknown";
    }
}
}
