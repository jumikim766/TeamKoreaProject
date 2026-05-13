package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.dto.BaseResponse;
import org.teamkorea.backend.dto.UrlDetailResponseDto;
import org.teamkorea.backend.dto.UrlListResponseDto;
import org.teamkorea.backend.service.UrlService;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    // URL 목록 조회
    @GetMapping
    public ResponseEntity<BaseResponse<UrlListResponseDto>> getUrls(
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) Boolean isAnalyzed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UrlListResponseDto response = urlService.getUrls(domain, riskLevel, isAnalyzed, page, size);

        return ResponseEntity.ok(
                BaseResponse.success("URL 목록 조회에 성공했습니다.", response)
        );
    }

    // URL 상세 조회
    @GetMapping("/{urlId}")
    public ResponseEntity<BaseResponse<UrlDetailResponseDto>> getUrlDetail(
            @PathVariable Long urlId
    ) {
        UrlDetailResponseDto response = urlService.getUrlDetail(urlId);

        return ResponseEntity.ok(
                BaseResponse.success("URL 상세 조회에 성공했습니다.", response)
        );
    }
}