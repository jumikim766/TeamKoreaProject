package org.teamkorea.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.dto.*;
import org.teamkorea.backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor // final 필드인 authService를 자동으로 생성자 주입해줌
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<SignupResponseDto>> signup(
            @Valid @RequestBody SignupRequestDto requestDto
    ) {
        SignupResponseDto response = authService.signup(requestDto);

        // API 명세서 기준: 201 Created + BaseResponse 형태로 응답
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto requestDto
    ) {
        LoginResponseDto response = authService.login(
                requestDto.getEmail(),
                requestDto.getPassword()
        );

        // API 명세서 기준: 200 OK + Access Token, Refresh Token 포함 응답
        return ResponseEntity.ok(
                BaseResponse.success("로그인에 성공했습니다.", response)
        );
    }

    @PostMapping("/reissue")
    public ResponseEntity<BaseResponse<ReissueResponseDto>> reissue(
           @Valid @RequestBody ReissueRequestDto requestDto
    ) {
        ReissueResponseDto response = authService.reissue(requestDto);

        // API 명세서 기준: refreshToken으로 accessToken 재발급
        return ResponseEntity.ok(
                BaseResponse.success("토큰이 재발급되었습니다.", response)
        );
    }
}