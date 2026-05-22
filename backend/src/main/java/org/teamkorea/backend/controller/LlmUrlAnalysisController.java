package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.ai.LlmAnalysisService;
import org.teamkorea.backend.ai.dto.LlmAnalysisResponse;
import org.teamkorea.backend.domain.Url;
import org.teamkorea.backend.dto.BaseResponse;
import org.teamkorea.backend.repository.UrlRepository;

@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LlmUrlAnalysisController {

    private final UrlRepository urlRepository;
    private final LlmAnalysisService llmAnalysisService;

    @GetMapping("/analyze/url/{urlId}")
    public ResponseEntity<BaseResponse<LlmAnalysisResponse>> analyzeUrl(
            @PathVariable Long urlId
    ) {
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new IllegalArgumentException("해당 URL을 찾을 수 없습니다. urlId=" + urlId));

        LlmAnalysisResponse response = llmAnalysisService.analyze(
                url.getNormalizedUrl(),
                url.getDomain(),
                null,
                0.0,
                false,
                false
        );

        return ResponseEntity.ok(
                BaseResponse.success("DB URL LLM 분석이 완료되었습니다.", response)
        );
    }
}