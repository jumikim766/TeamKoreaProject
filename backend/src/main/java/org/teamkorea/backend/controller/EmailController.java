package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.dto.BaseResponse;
import org.teamkorea.backend.dto.EmailDetailResponseDto;
import org.teamkorea.backend.dto.EmailListResponseDto;
import org.teamkorea.backend.dto.EmailUrlResponseDto;
import org.teamkorea.backend.service.EmailService;
import org.springframework.security.core.Authentication;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
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
        public ResponseEntity<BaseResponse<Map<String, Object>>> getEmails(
                @RequestParam(required = false) Long accountId,
                @RequestParam(required = false) String keyword,
                @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                LocalDateTime receivedAtFrom,
                @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                LocalDateTime receivedAtTo,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "20") int size,
                Authentication authentication
        ) {
                // JWT에서 현재 로그인한 사용자 ID 추출
                Long userId = getLoginUserId(authentication);

                // 현재 사용자 기준 + 검색 조건으로 이메일 목록 조회
                Page<EmailListResponseDto> emailPage =
                emailService.getEmails(userId, accountId, keyword, receivedAtFrom, receivedAtTo, page, size);

                // API 명세서 응답 형태에 맞게 data 구성
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("emails", emailPage.getContent());
                data.put("page", emailPage.getNumber());
                data.put("size", emailPage.getSize());
                data.put("totalElements", emailPage.getTotalElements());
                data.put("totalPages", emailPage.getTotalPages());

                return ResponseEntity.ok(
                        BaseResponse.success("이메일 목록 조회에 성공했습니다.", data)
                );
        }

        // 이메일 상세 조회
        @GetMapping("/{emailId}")
        public ResponseEntity<BaseResponse<EmailDetailResponseDto>> getEmailDetail(
                @PathVariable Long emailId,
                Authentication authentication
        ) {
        // JWT에서 현재 로그인한 사용자 ID 추출
        Long userId = getLoginUserId(authentication);

        // 현재 사용자 소유 이메일인지 검증 후 상세 조회
        EmailDetailResponseDto data = emailService.getEmailDetail(userId, emailId);

        return ResponseEntity.ok(
            BaseResponse.success("이메일 상세 조회에 성공했습니다.", data)
        );      }


        // 특정 이메일에서 추출된 URL 목록 조회
        @GetMapping("/{emailId}/urls")
        public ResponseEntity<BaseResponse<Map<String, Object>>> getEmailUrls(
                @PathVariable Long emailId,
                Authentication authentication
        ) {
        // JWT에서 현재 로그인한 사용자 ID 추출
        Long userId = getLoginUserId(authentication);

        // 현재 사용자 소유 이메일인지 검증 후 URL 목록 조회
        List<EmailUrlResponseDto> urls = emailService.getEmailUrls(userId, emailId);

        // API 명세서 응답 형태에 맞게 data 구성
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("emailId", emailId);
        data.put("urls", urls);

        return ResponseEntity.ok(
            BaseResponse.success("이메일 URL 목록 조회에 성공했습니다.", data)
        );      }


        // JWT 인증 정보에서 로그인 사용자 ID를 꺼내는 메서드
        private Long getLoginUserId(Authentication authentication) {
        // 인증 정보가 없으면 로그인 필요 처리
        if (authentication == null || authentication.getDetails() == null) {
                throw new IllegalStateException("로그인이 필요합니다.");
        }

        // JwtAuthenticationFilter에서 details에 userId를 넣어둔 구조
        return (Long) authentication.getDetails();
        }
}