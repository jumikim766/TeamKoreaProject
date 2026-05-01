package org.teamkorea.backend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.teamkorea.backend.domain.User;
import org.teamkorea.backend.repository.UserRepository;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public OAuth2SuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
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

            Map<String, Object> naverResponse =
                    (Map<String, Object>) attributes.get("response");

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
            throw new IllegalStateException("사용자 정보가 부족합니다.");
        }

        User user = userRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElseGet(() ->
                        userRepository.findByEmail(email).orElseGet(() ->
                                User.builder()
                                        .username(generateUsername(email, providerId))
                                        .email(email)
                                        .name(name)
                                        // 소셜 로그인은 일반 비밀번호가 없으므로 임시값 저장
                                        .passwordHash("SOCIAL_LOGIN")
                                        .role("USER")
                                        .status("ACTIVE")
                                        .provider(provider)
                                        .providerId(providerId)
                                        .build()
                        )
                );

        String username = generateUsername(email, providerId);

        // setter 대신 User 엔티티 내부 메서드 사용
        user.updateOAuthInfo(username, email, name, provider, providerId);

        userRepository.save(user);
    //(추후 React로 변경 예정/response.sendRedirect("http://localhost:5173/login-success");)
        response.sendRedirect("http://localhost:5173");
    }

    private String generateUsername(String email, String providerId) {
        String base = email.split("@")[0];
        String suffix = providerId.substring(0, Math.min(5, providerId.length()));
        String username = base + "_" + suffix;

        if (username.length() > 20) {
            username = username.substring(0, 20);
        }

        return username;
    }
}