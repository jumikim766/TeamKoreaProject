package org.teamkorea.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.dto.BaseResponse;
import org.teamkorea.backend.dto.EmailAccountRequestDto;
import org.teamkorea.backend.dto.EmailAccountResponseDto;
import org.teamkorea.backend.service.EmailAccountService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email-accounts")
@RequiredArgsConstructor
public class EmailAccountController {

    private final EmailAccountService emailAccountService;

    // 이메일 계정 등록
    @PostMapping
    public ResponseEntity<BaseResponse<EmailAccountResponseDto>> createEmailAccount(
            @Valid @RequestBody EmailAccountRequestDto request,
            Authentication authentication
    ) {
        Long userId = getLoginUserId(authentication);

        EmailAccountResponseDto response =
                emailAccountService.createEmailAccount(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success("이메일 계정이 연동되었습니다.", response));
    }

    // 이메일 계정 목록 조회
    @GetMapping
    public ResponseEntity<BaseResponse<Map<String, Object>>> getEmailAccounts(
            Authentication authentication
    ) {
        Long userId = getLoginUserId(authentication);

        List<EmailAccountResponseDto> list =
                emailAccountService.getEmailAccounts(userId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("emailAccounts", list);

        return ResponseEntity.ok(
                BaseResponse.success("이메일 연동 계정 목록 조회에 성공했습니다.", data)
        );
    }

    // 이메일 계정 삭제
    @DeleteMapping("/{accountId}")
    public ResponseEntity<BaseResponse<Void>> deleteEmailAccount(
            @PathVariable Long accountId,
            Authentication authentication
    ) {
        Long userId = getLoginUserId(authentication);

        emailAccountService.deleteEmailAccount(userId, accountId);

        return ResponseEntity.ok(
                BaseResponse.success("이메일 계정이 삭제되었습니다.")
        );
    }

    // 이메일 즉시 동기화
    @PostMapping("/{accountId}/sync")
    public ResponseEntity<BaseResponse<Map<String, Object>>> syncEmails(
            @PathVariable Long accountId,
            Authentication authentication
    ) {
        Long userId = getLoginUserId(authentication);

        Map<String, Object> syncResult =
                emailAccountService.syncEmails(userId, accountId);

        return ResponseEntity.ok(
                BaseResponse.success("이메일 동기화가 완료되었습니다.", syncResult)
        );
    }

    // JWT 인증 객체에서 userId 추출
    private Long getLoginUserId(Authentication authentication) {

        if (authentication == null || authentication.getDetails() == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        return (Long) authentication.getDetails();
    }
}