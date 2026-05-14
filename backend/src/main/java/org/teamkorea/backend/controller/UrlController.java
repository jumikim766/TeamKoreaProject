package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.dto.BaseResponse;
import org.teamkorea.backend.dto.UrlDetailResponseDto;
import org.teamkorea.backend.dto.UrlListResponseDto;
import org.teamkorea.backend.dto.UrlStatisticsResponseDto;
import org.teamkorea.backend.exception.BusinessException;
import org.teamkorea.backend.exception.ErrorCode;
import org.teamkorea.backend.service.UrlService;
import org.springframework.security.core.Authentication;
import org.teamkorea.backend.dto.MyUrlListResponseDto;
import org.teamkorea.backend.dto.UrlStatisticsResponseDto;

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
            @RequestParam(defaultValue = "20") int size) {
        UrlListResponseDto response = urlService.getUrls(domain, riskLevel, isAnalyzed, page, size);

        return ResponseEntity.ok(
                BaseResponse.success("URL 목록 조회에 성공했습니다.", response));
    }

    // URL 위험도 통계 조회
    @GetMapping("/statistics")
    public ResponseEntity<BaseResponse<UrlStatisticsResponseDto>> getUrlStatistics(
            @RequestParam(defaultValue = "ALL") String scope,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) Boolean isAnalyzed,
            Authentication authentication) {
        Long userId = getLoginUserId(authentication);

        // scope=MY이면 현재 로그인한 사용자의 URL 기준으로 통계 조회
        if ("MY".equalsIgnoreCase(scope)) {
            userId = getLoginUserId(authentication);
        }

        UrlStatisticsResponseDto response = urlService.getUrlStatistics(
                userId,
                scope,
                accountId,
                domain,
                isAnalyzed);

        return ResponseEntity.ok(
                BaseResponse.success("URL 위험도 통계 조회에 성공했습니다.", response));
    }

    // URL 상세 조회
    @GetMapping("/{urlId}")
    public ResponseEntity<BaseResponse<UrlDetailResponseDto>> getUrlDetail(
            @PathVariable Long urlId) {
        UrlDetailResponseDto response = urlService.getUrlDetail(urlId);

        return ResponseEntity.ok(
                BaseResponse.success("URL 상세 조회에 성공했습니다.", response));
    }

    // JwtAuthenticationFilter에서 authentication.details에 저장한 userId 꺼내기
    private Long getLoginUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof Long userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        // return userId;
        return (Long) authentication.getDetails();
    }
}