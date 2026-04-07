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
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public OAuth2SuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 로그인 성공 시
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        System.out.println("=== OAuth2SuccessHandler 실행됨 ===");

        // 사용자 정보
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        System.out.println("OAuth attributes = " + attributes);

        String provider;
        String providerId;
        String email;
        String name;

        // 네이버 로그인 ( response 있으면 네이버 / 없으면 구글 )
        if (attributes.containsKey("response")) {
            provider = "NAVER";

            Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");

            providerId = (String) naverResponse.get("id");
            email = (String) naverResponse.get("email");
            name = (String) naverResponse.get("name");
        }

        // 구글 로그인
        else {
            provider = "GOOGLE";

            providerId = (String) attributes.get("sub");
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        }

        System.out.println("provider = " + provider);
        System.out.println("providerId = " + providerId);
        System.out.println("email = " + email);
        System.out.println("name = " + name);

        /* 
        if (providerId == null) {
            throw new IllegalStateException("providerId가 없습니다.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("email이 없습니다.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("name이 없습니다.");
        }*/

        // 값 없으면 에러 -> 로그인 실패 처리
        if (providerId == null || email == null || name == null) {
            throw new IllegalStateException("사용자 정보가 부족합니다.");//(추후 React로 연동)
        }

        // 기존 회원 조회
        User user = userRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElseGet(() ->
                        userRepository.findByEmail(email).orElseGet(User::new)
                );

        // 신규 유저면 username 생성
        if (user.getUserId() == null) {
            String username = generateUsername(email, providerId);
            user.setUsername(username);
        }

        // 공통 정보 저장
        user.setEmail(email);
        user.setName(name);
        user.setProvider(provider);
        user.setProviderId(providerId);
        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setLastLoginAt(LocalDateTime.now());

        // DB 저장
        userRepository.save(user);

        //(추후 React로 변경 예정/response.sendRedirect("http://localhost:5173/login-success");)
        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().write("로그인 성공: " + email + " / provider: " + provider);
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