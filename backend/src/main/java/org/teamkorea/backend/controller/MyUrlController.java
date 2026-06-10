package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.dto.BaseResponse;
import org.teamkorea.backend.dto.MyUrlListResponseDto;
import org.teamkorea.backend.exception.BusinessException;
import org.teamkorea.backend.exception.ErrorCode;
import org.teamkorea.backend.service.UrlService;


@RestController
@RequestMapping("/api/my-urls")
@RequiredArgsConstructor
public class MyUrlController {

    private final UrlService urlService;

    // 나의 URL 목록 조회
    @GetMapping
    public ResponseEntity<BaseResponse<MyUrlListResponseDto>> getMyUrls(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) Boolean isAnalyzed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        Long userId = getLoginUserId(authentication);

        MyUrlListResponseDto response = urlService.getMyUrls(
                userId,
                accountId,
                domain,
                riskLevel,
                isAnalyzed,
                page,
                size
        );

        return ResponseEntity.ok(
                BaseResponse.success("나의 URL 목록 조회에 성공했습니다.", response)
        );
    }

    // JWT 인증 정보에서 로그인 사용자 ID 추출
    private Long getLoginUserId(Authentication authentication) {

        // 인증 정보가 없으면 401 처리
        if (authentication == null || authentication.getDetails() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        // userId 타입 검증
        if (!(authentication.getDetails() instanceof Long userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증 정보가 올바르지 않습니다.");
        }

        return userId;
    }
}