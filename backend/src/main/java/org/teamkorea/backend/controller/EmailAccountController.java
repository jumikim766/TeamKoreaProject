package org.teamkorea.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.dto.EmailAccountRequestDto;
import org.teamkorea.backend.dto.EmailAccountResponse;
import org.teamkorea.backend.service.EmailAccountService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email-accounts")
@RequiredArgsConstructor
public class EmailAccountController {

    private final EmailAccountService emailAccountService;

    // 임시 테스트용 userId
    private Long getCurrentUserId() {
        return 1L;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createEmailAccount(
            @Valid @RequestBody EmailAccountRequestDto request
    ) {
        EmailAccountResponse response =
                emailAccountService.createEmailAccount(getCurrentUserId(), request);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "이메일 계정이 연동되었습니다.");
        body.put("data", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getEmailAccounts() {
        List<EmailAccountResponse> accounts =
                emailAccountService.getEmailAccounts(getCurrentUserId());

        Map<String, Object> data = new HashMap<>();
        data.put("emailAccounts", accounts);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "이메일 연동 계정 목록 조회에 성공했습니다.");
        body.put("data", data);

        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Map<String, Object>> deleteEmailAccount(
            @PathVariable Long accountId
    ) {
        emailAccountService.deleteEmailAccount(getCurrentUserId(), accountId);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "이메일 계정이 삭제되었습니다.");
        body.put("data", null);

        return ResponseEntity.ok(body);
    }
}