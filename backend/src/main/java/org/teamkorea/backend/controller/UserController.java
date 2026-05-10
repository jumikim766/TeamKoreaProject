package org.teamkorea.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.dto.*;
import org.teamkorea.backend.exception.BusinessException;
import org.teamkorea.backend.exception.ErrorCode;
import org.teamkorea.backend.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

        private final UserService userService;

        @GetMapping("/me")
        public ResponseEntity<BaseResponse<UserMeResponseDto>> getMyInfo(Authentication authentication) {
                Long userId = extractUserId(authentication);
                return ResponseEntity.ok(
                        BaseResponse.success("내 정보 조회 성공", userService.getMyInfo(userId)));
        }

        @PatchMapping("/me")
        public ResponseEntity<BaseResponse<UserUpdateResponseDto>> updateMyInfo(
                Authentication authentication,
                @Valid @RequestBody UserUpdateRequestDto requestDto
        ) {
                Long userId = extractUserId(authentication);
                return ResponseEntity.ok(
                        BaseResponse.success("내 정보가 수정되었습니다.", userService.updateMyInfo(userId, requestDto)));
        }

        @PatchMapping("/me/password")
        public ResponseEntity<BaseResponse<Void>> changePassword(
                Authentication authentication,
                @Valid @RequestBody PasswordChangeRequestDto requestDto
        ) {
                Long userId = extractUserId(authentication);
                userService.changePassword(userId, requestDto);
                return ResponseEntity.ok(BaseResponse.success("비밀번호가 변경되었습니다."));
        }

        @DeleteMapping("/me")
        public ResponseEntity<BaseResponse<Void>> deleteMyAccount(
                Authentication authentication,
                @Valid @RequestBody UserDeleteRequestDto requestDto
        ) {
                Long userId = extractUserId(authentication);
                userService.deleteMyAccount(userId, requestDto);
                return ResponseEntity.ok(BaseResponse.success("회원 탈퇴가 완료되었습니다."));
        }

        @GetMapping("/check-username")
        public ResponseEntity<BaseResponse<DuplicateCheckResponseDto>> checkUsername(
                @RequestParam String username
        ) {
                return ResponseEntity.ok(
                        BaseResponse.success("아이디 중복 체크 성공", userService.checkUsername(username)));
        }

        @GetMapping("/check-email")
        public ResponseEntity<BaseResponse<DuplicateCheckResponseDto>> checkEmail(
                @RequestParam String email
        ) {
                return ResponseEntity.ok(
                        BaseResponse.success("이메일 중복 체크 성공", userService.checkEmail(email)));
        }

    /**
     * Authentication details에서 userId 추출.
     * 인증 객체가 없으면 401(UNAUTHORIZED) → GlobalExceptionHandler가 처리.
     * (기존: IllegalArgumentException → 400으로 처리되던 버그 수정)
     */
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getDetails() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        if (!(authentication.getDetails() instanceof Long)) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증 정보가 올바르지 않습니다.");
        }

        return (Long) authentication.getDetails();
    }
}
