package org.teamkorea.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.dto.*;
import org.teamkorea.backend.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ===== 기존 유지/수정: JWT에서 userId를 꺼내 내 정보 조회 =====
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserMeResponseDto>> getMyInfo(Authentication authentication) {
        Long userId = getUserId(authentication);

        UserMeResponseDto response = userService.getMyInfo(userId);

        return ResponseEntity.ok(
                BaseResponse.success("내 정보 조회 성공", response)
        );
    }

    // ===== 추가: 회원정보 수정 API =====
    @PatchMapping("/me")
    public ResponseEntity<BaseResponse<UserUpdateResponseDto>> updateMyInfo(
            Authentication authentication,
            @Valid @RequestBody UserUpdateRequestDto requestDto
    ) {
        Long userId = getUserId(authentication);

        UserUpdateResponseDto response = userService.updateMyInfo(userId, requestDto);

        return ResponseEntity.ok(
                BaseResponse.success("내 정보가 수정되었습니다.", response)
        );
    }

    // ===== 추가: 비밀번호 변경 API =====
    @PatchMapping("/me/password")
    public ResponseEntity<BaseResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody PasswordChangeRequestDto requestDto
    ) {
        Long userId = getUserId(authentication);

        userService.changePassword(userId, requestDto);

        return ResponseEntity.ok(
                BaseResponse.success("비밀번호가 변경되었습니다.")
        );
    }

    // ===== 추가: 회원탈퇴 API - soft delete 방식 =====
    @DeleteMapping("/me")
    public ResponseEntity<BaseResponse<Void>> deleteMyAccount(
            Authentication authentication,
            @Valid @RequestBody UserDeleteRequestDto requestDto
    ) {
        Long userId = getUserId(authentication);

        userService.deleteMyAccount(userId, requestDto);

        return ResponseEntity.ok(
                BaseResponse.success("회원 탈퇴가 완료되었습니다.")
        );
    }

    // ===== 추가: username 중복 체크 API =====
    @GetMapping("/check-username")
    public ResponseEntity<BaseResponse<DuplicateCheckResponseDto>> checkUsername(
            @RequestParam String username
    ) {
        DuplicateCheckResponseDto response = userService.checkUsername(username);

        return ResponseEntity.ok(
                BaseResponse.success("아이디 중복 체크 성공", response)
        );
    }

    // ===== 추가: email 중복 체크 API =====
    @GetMapping("/check-email")
    public ResponseEntity<BaseResponse<DuplicateCheckResponseDto>> checkEmail(
            @RequestParam String email
    ) {
        DuplicateCheckResponseDto response = userService.checkEmail(email);

        return ResponseEntity.ok(
                BaseResponse.success("이메일 중복 체크 성공", response)
        );
    }

    // ===== 추가: Authentication에서 userId 추출 공통 메서드 =====
    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getDetails() == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        return (Long) authentication.getDetails();
    }
}