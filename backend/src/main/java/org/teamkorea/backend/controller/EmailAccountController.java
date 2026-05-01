package org.teamkorea.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.dto.BaseResponse;
import org.teamkorea.backend.dto.EmailAccountRequestDto;
import org.teamkorea.backend.dto.EmailAccountResponse;
import org.teamkorea.backend.service.EmailAccountService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email-accounts")
@RequiredArgsConstructor // final 필드인 emailAccountService를 생성자 주입
public class EmailAccountController {

    private final EmailAccountService emailAccountService;

    // TODO: JWT 연동 후 SecurityContextHolder에서 실제 로그인한 userId를 가져오도록 수정 필요
    private Long getCurrentUserId() {
        return 1L;
    }

    @PostMapping
    public ResponseEntity<BaseResponse<EmailAccountResponse>> createEmailAccount(
            @Valid @RequestBody EmailAccountRequestDto request
    ) {
        // API 명세서 기준 요청값: provider, email, imapHost, imapPort, loginId, password
        EmailAccountResponse response =
                emailAccountService.createEmailAccount(getCurrentUserId(), request);

        // API 명세서 기준: 201 Created + 이메일 계정 연동 성공 응답
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("이메일 계정이 연동되었습니다.", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<Map<String, List<EmailAccountResponse>>>> getEmailAccounts() {
        List<EmailAccountResponse> accounts =
                emailAccountService.getEmailAccounts(getCurrentUserId());

        // API 명세서 기준: data 안에 emailAccounts 배열로 감싸서 반환
        Map<String, List<EmailAccountResponse>> data = new HashMap<>();
        data.put("emailAccounts", accounts);

        return ResponseEntity.ok(
                BaseResponse.success("이메일 연동 계정 목록 조회에 성공했습니다.", data)
        );
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<BaseResponse<Void>> deleteEmailAccount(
            @PathVariable Long accountId
    ) {
        // accountId 기준으로 현재 사용자의 이메일 연동 계정 삭제
        emailAccountService.deleteEmailAccount(getCurrentUserId(), accountId);

        // 삭제 성공 시 data는 null
        return ResponseEntity.ok(
                BaseResponse.success("이메일 계정이 삭제되었습니다.", null)
        );
    }
}