package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.dto.EmailDetailResponseDto;
import org.teamkorea.backend.dto.EmailListResponseDto;
import org.teamkorea.backend.dto.EmailUrlResponseDto;
import org.teamkorea.backend.service.EmailService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    // 이메일 목록 조회
    @GetMapping
    public ResponseEntity<?> getEmails(
            @RequestParam(required = false) Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<EmailListResponseDto> emailPage = emailService.getEmails(accountId, page, size);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("emails", emailPage.getContent());
        data.put("page", emailPage.getNumber());
        data.put("size", emailPage.getSize());
        data.put("totalElements", emailPage.getTotalElements());
        data.put("totalPages", emailPage.getTotalPages());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "이메일 목록 조회에 성공했습니다.");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    // 이메일 상세 조회
    @GetMapping("/{emailId}")
    public ResponseEntity<?> getEmailDetail(@PathVariable Long emailId) {

        EmailDetailResponseDto data = emailService.getEmailDetail(emailId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "이메일 상세 조회에 성공했습니다.");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    // 특정 이메일의 추출 URL 목록 조회
    @GetMapping("/{emailId}/urls")
    public ResponseEntity<?> getEmailUrls(@PathVariable Long emailId) {

        List<EmailUrlResponseDto> urls = emailService.getEmailUrls(emailId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("emailId", emailId);
        data.put("urls", urls);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "이메일 URL 목록 조회에 성공했습니다.");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}