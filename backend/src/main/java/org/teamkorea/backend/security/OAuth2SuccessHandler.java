package org.teamkorea.backend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.teamkorea.backend.domain.RefreshToken;
import org.teamkorea.backend.domain.User;
import org.teamkorea.backend.exception.BusinessException;
import org.teamkorea.backend.exception.ErrorCode;
import org.teamkorea.backend.repository.RefreshTokenRepository;
import org.teamkorea.backend.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    public OAuth2SuccessHandler(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String provider;
        String providerId;
        String email;
        String name;

        if (attributes.containsKey("response")) {
            provider = "NAVER";
            Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
            providerId = (String) naverResponse.get("id");
            email      = (String) naverResponse.get("email");
            name       = (String) naverResponse.get("name");
        } else {
            provider   = "GOOGLE";
            providerId = (String) attributes.get("sub");
            email      = (String) attributes.get("email");
            name       = (String) attributes.get("name");
        }

        if (providerId == null || email == null || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "소셜 로그인 사용자 정보가 부족합니다.");
        }

        User user = userRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElseGet(() ->
                        userRepository.findByEmail(email).orElseGet(() ->
                                User.builder()
                                        .username(generateUsername(email, providerId))
                                        .email(email)
                                        .name(name)
                                        .passwordHash(null)
                                        .role("USER")
                                        .status("ACTIVE")
                                        .provider(provider)
                                        .providerId(providerId)
                                        .build()
                        )
                );

        user.updateOAuthInfo(generateUsername(email, providerId), email, name, provider, providerId);
        user.updateLastLoginAt();
        User savedUser = userRepository.save(user);

        String accessToken  = jwtUtil.generateAccessToken(savedUser);
        String refreshToken = jwtUtil.generateRefreshToken(savedUser);
        String refreshTokenHash = jwtUtil.hashToken(refreshToken);

        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                jwtUtil.getRefreshTokenExpiryInstant(), ZoneId.systemDefault());

        refreshTokenRepository.deleteAllByUser(savedUser);
        refreshTokenRepository.save(new RefreshToken(savedUser, refreshTokenHash, expiresAt));

        /*
         * TODO [보안 개선 필요] — 프로덕션 배포 전 반드시 수정할 것
         *
         * 현재: Access/Refresh Token을 URL 쿼리 파라미터로 전달
         *   → 브라우저 주소창 노출, HTTP Referer 헤더 유출, 브라우저 히스토리에 기록됨
         *
         * 권장 방식 (택1):
         *   1) HttpOnly + Secure 쿠키로 토큰 전달
         *      response.addCookie(makeHttpOnlyCookie("refreshToken", refreshToken));
         *      response.sendRedirect("http://localhost:5173/oauth/callback");
         *
         *   2) Authorization Code(일회용 코드) 방식
         *      서버에서 단기 코드를 발급 → 프론트가 코드로 토큰 교환 요청
         */
        String redirectUrl = "http://localhost:5173/oauth/callback"
                + "?accessToken="  + URLEncoder.encode(accessToken,  StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
                + "&tokenType=Bearer";

        response.sendRedirect(redirectUrl);
    }

    private String generateUsername(String email, String providerId) {
        String base   = email.split("@")[0];
        String suffix = providerId.substring(0, Math.min(5, providerId.length()));
        String username = base + "_" + suffix;
        return username.length() > 20 ? username.substring(0, 20) : username;
    }
}
