package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.domain.EmailAccount;
import org.teamkorea.backend.dto.BaseResponse;
import org.teamkorea.backend.repository.EmailAccountRepository;
import org.teamkorea.backend.service.EmailSaveService;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestMailAnalysisController {

    private final EmailSaveService emailSaveService;
    private final EmailAccountRepository emailAccountRepository;

    @GetMapping("/mail-analysis")
    public ResponseEntity<BaseResponse<Integer>> testMailAnalysis(
            @RequestParam Long userId,
            @RequestParam Long accountId
    ) {
        EmailAccount account = emailAccountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("메일 계정을 찾을 수 없습니다."));

        int count = emailSaveService.saveEmailAndUrls(
                userId,
                account,
                "test-message-uid-" + System.currentTimeMillis(),
                "테스트 발신자",
                "attacker@example.com",
                "user@example.com",
                "[긴급] 계정 보안 인증이 필요합니다",
                "보안 문제로 인해 아래 링크에서 즉시 로그인 인증을 진행하세요. http://secure-login-bank-update.fake-site.xyz/login",
                "<p>보안 문제로 인해 아래 링크에서 즉시 로그인 인증을 진행하세요.</p><a href=\"http://secure-login-bank-update.fake-site.xyz/login\">인증하기</a>",
                LocalDateTime.now(),
                List.of("http://secure-login-bank-update.fake-site.xyz/login")
        );

        return ResponseEntity.ok(
                BaseResponse.success("테스트 메일 URL 자동 분석이 완료되었습니다.", count)
        );
    }
}