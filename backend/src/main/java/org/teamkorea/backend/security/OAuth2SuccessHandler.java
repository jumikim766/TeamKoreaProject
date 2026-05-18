package org.teamkorea.backend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.RefreshToken;
import org.teamkorea.backend.domain.User;
import org.teamkorea.backend.exception.BusinessException;
import org.teamkorea.backend.exception.ErrorCode;
import org.teamkorea.backend.repository.RefreshTokenRepository;
import org.teamkorea.backend.repository.UserRepository;
import java.util.UUID;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Value("${app.oauth2.redirect-uri}")
    private String frontRedirectUri; // ex) http://localhost:5173/oauth/callback

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMillis;

    @Value("${app.cookie.secure:false}") // 로컬은 false, 운영은 true
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}") // 로컬은 Lax, 크로스도메인 운영은 None
    private String cookieSameSite;

    @Value("${app.cookie.domain:}") // 로컬은 비움
    private String cookieDomain;

    public OAuth2SuccessHandler(UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtUtil jwtUtil) {
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

        String provider, providerId, email, name;
        if (attributes.containsKey("response")) {
            provider = "NAVER";
            Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
            providerId = (String) naverResponse.get("id");
            email = (String) naverResponse.get("email");
            name = (String) naverResponse.get("name");
        } else {
            provider = "GOOGLE";
            providerId = (String) attributes.get("sub");
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        }

        if (providerId == null || email == null || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "소셜 로그인 사용자 정보가 부족합니다.");
        }

        User user = userRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> userRepository.findByEmail(email).orElseGet(() -> User.builder()
                        .username(generateUsername(email))
                        .email(email)
                        .name(name)
                        .passwordHash(null)
                        .role("USER")
                        .status("ACTIVE")
                        .provider(provider)
                        .providerId(providerId)
                        .build()));

        user.updateOAuthInfo(user.getUsername(), email, name, provider, providerId);
        user.updateLastLoginAt();

        User savedUser = userRepository.save(user);

        String refreshToken = jwtUtil.generateRefreshToken(savedUser);
        String refreshTokenHash = jwtUtil.hashToken(refreshToken);

        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                jwtUtil.getRefreshTokenExpiryInstant(), ZoneId.systemDefault());

        refreshTokenRepository.deleteAllByUser(savedUser);
        refreshTokenRepository.save(new RefreshToken(savedUser, refreshTokenHash, expiresAt));

        // === HttpOnly + Secure 쿠키로 refreshToken 전달 ===
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/") // 또는 "/api/auth"로 좁혀도 됨
                .maxAge(Duration.ofMillis(refreshTokenExpirationMillis));
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
        response.addHeader("Set-Cookie", builder.build().toString());

        // accessToken은 URL에 싣지 않는다 → 프론트가 /api/auth/reissue로 받아감
        response.sendRedirect(frontRedirectUri);
    }

    private String generateUsername(String email) {
        String base = email.split("@")[0]
                .replaceAll("[^a-zA-Z0-9]", "");

        String suffix = UUID.randomUUID().toString().substring(0, 8);

        String username = base + "_" + suffix;

        return username.length() > 20 ? username.substring(0, 20) : username;
    }
}