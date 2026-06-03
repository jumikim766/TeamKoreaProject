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
import org.teamkorea.backend.exception.BusinessException;
import org.teamkorea.backend.exception.ErrorCode;
import org.teamkorea.backend.service.EmailSyncAsyncService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email-accounts")
@RequiredArgsConstructor
public class EmailAccountController {

        private final EmailAccountService emailAccountService;
        private final EmailSyncAsyncService emailSyncAsyncService;

        // 이메일 계정 등록
        @PostMapping
        public ResponseEntity<BaseResponse<EmailAccountResponseDto>> createEmailAccount(
                        @Valid @RequestBody EmailAccountRequestDto request,
                        Authentication authentication) {
                Long userId = getLoginUserId(authentication);

                EmailAccountResponseDto response = emailAccountService.createEmailAccount(userId, request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(BaseResponse.success("이메일 계정이 연동되었습니다.", response));
        }

        // 이메일 계정 목록 조회
        @GetMapping
        public ResponseEntity<BaseResponse<Map<String, Object>>> getEmailAccounts(
                        Authentication authentication) {
                Long userId = getLoginUserId(authentication);

                List<EmailAccountResponseDto> list = emailAccountService.getEmailAccounts(userId);

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("emailAccounts", list);

                return ResponseEntity.ok(
                                BaseResponse.success("이메일 연동 계정 목록 조회에 성공했습니다.", data));
        }

        // 이메일 계정 삭제
        @DeleteMapping("/{accountId}")
        public ResponseEntity<BaseResponse<Void>> deleteEmailAccount(
                        @PathVariable Long accountId,
                        Authentication authentication) {
                Long userId = getLoginUserId(authentication);

                emailAccountService.deleteEmailAccount(userId, accountId);

                return ResponseEntity.ok(
                                BaseResponse.success("이메일 계정이 삭제되었습니다."));
        }

        // 이메일 즉시 동기화 요청
        // 사용자는 바로 응답 받고, 실제 동기화는 서버 백그라운드에서 실행
        @PostMapping("/{accountId}/sync")
        public ResponseEntity<BaseResponse<Map<String, Object>>> syncEmails(
                        @PathVariable Long accountId,
                        Authentication authentication) {
                Long userId = getLoginUserId(authentication);

                // 계정 존재/권한/active 여부만 먼저 검증
                emailAccountService.validateSyncRequest(userId, accountId);

                // 실제 sync는 비동기로 실행
                emailSyncAsyncService.syncEmailsInBackground(userId, accountId);

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("accountId", accountId);
                data.put("status", "PROCESSING");

                return ResponseEntity.ok(
                                BaseResponse.success("이메일 동기화 요청이 접수되었습니다.", data));
        }

        // JWT 인증 객체에서 userId 추출
        private Long getLoginUserId(Authentication authentication) {

                if (authentication == null || authentication.getDetails() == null) {
                        throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");

                }

                if (!(authentication.getDetails() instanceof Long userId)) {
                        throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증 정보가 올바르지 않습니다.");
                }

                return userId;

        }
}