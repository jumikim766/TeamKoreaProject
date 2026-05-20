package org.teamkorea.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.dto.*;
import org.teamkorea.backend.service.AuthService;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthService authService;

        @Value("${jwt.refresh-token-expiration}")
        private long refreshTokenExpirationMillis;

        @Value("${app.cookie.secure:false}")
        private boolean cookieSecure;

        @Value("${app.cookie.same-site:Lax}")
        private String cookieSameSite;

        @Value("${app.cookie.domain:}")
        private String cookieDomain;

        @PostMapping("/signup")
        public ResponseEntity<BaseResponse<SignupResponseDto>> signup(
                        @Valid @RequestBody SignupRequestDto requestDto) {
                SignupResponseDto response = authService.signup(requestDto);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(BaseResponse.success("회원가입이 완료되었습니다.", response));
        }

        /** 일반 로그인: accessToken은 바디로, refreshToken은 HttpOnly 쿠키로 */
        @PostMapping("/login")
        public ResponseEntity<BaseResponse<LoginResponseDto>> login(
                        @Valid @RequestBody LoginRequestDto requestDto) {
                LoginResponseDto loginResponse = authService.login(
                                requestDto.getUsername(),
                                requestDto.getPassword());

                String setCookie = buildRefreshCookie(
                                loginResponse.getRefreshToken(),
                                Duration.ofMillis(refreshTokenExpirationMillis));

                LoginResponseDto responseBody = LoginResponseDto.builder()
                                .accessToken(loginResponse.getAccessToken())
                                .refreshToken(null)
                                .tokenType(loginResponse.getTokenType())
                                .user(loginResponse.getUser())
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, setCookie)
                                .body(BaseResponse.success("로그인에 성공했습니다.", responseBody));
        }

        /** 쿠키의 refreshToken을 사용해 accessToken 재발급 */
        @PostMapping("/reissue")
        public ResponseEntity<BaseResponse<ReissueResponseDto>> reissue(
                        @CookieValue(name = "refreshToken", required = false) String refreshToken) {
                ReissueResponseDto reissueResponse = authService.reissue(refreshToken);

                String setCookie = buildRefreshCookie(
                                reissueResponse.getRefreshToken(),
                                Duration.ofMillis(refreshTokenExpirationMillis));

                ReissueResponseDto responseBody = ReissueResponseDto.builder()
                                .accessToken(reissueResponse.getAccessToken())
                                .refreshToken(null)
                                .tokenType(reissueResponse.getTokenType())
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, setCookie)
                                .body(BaseResponse.success("토큰이 재발급되었습니다.", responseBody));
        }

        /** 쿠키의 refreshToken을 받아 무효화하고, 쿠키도 만료 */
        @PostMapping("/logout")
        public ResponseEntity<BaseResponse<Void>> logout(
                        @CookieValue(name = "refreshToken", required = false) String refreshToken) {
                authService.logout(refreshToken);

                String expiredCookie = buildRefreshCookie("", Duration.ZERO);

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, expiredCookie)
                                .body(BaseResponse.success("로그아웃되었습니다.", null));
        }

        private String buildRefreshCookie(String value, Duration maxAge) {
                ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("refreshToken", value)
                                .httpOnly(true)
                                .secure(cookieSecure)
                                .sameSite(cookieSameSite)
                                .path("/")
                                .maxAge(maxAge);

                if (cookieDomain != null && !cookieDomain.isBlank()) {
                        cookieBuilder.domain(cookieDomain);
                }

                return cookieBuilder.build().toString();
        }
}