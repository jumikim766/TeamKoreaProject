package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.teamkorea.backend.dto.BaseResponse;
import org.teamkorea.backend.dto.UserMeResponseDto;
import org.teamkorea.backend.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserMeResponseDto>> getMyInfo(Authentication authentication) {

        // JwtAuthenticationFilter에서 principal에는 email, details에는 userId를 저장함
        Long userId = (Long) authentication.getDetails();

        UserMeResponseDto response = userService.getMyInfo(userId);

        return ResponseEntity.ok(
                BaseResponse.success("사용자 정보 조회 성공", response)
        );
    }
}