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

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public OAuth2SuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String sub = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        System.out.println("==== OAuth2SuccessHandler 진입 ====");
        System.out.println("sub = " + sub);
        System.out.println("email = " + email);
        System.out.println("name = " + name);

        if (sub == null || email == null || name == null) {
            throw new IllegalStateException("Google OAuth 사용자 정보가 부족합니다.");
        }

        User user = userRepository
                .findUserByProviderAndProviderId("google", sub)
                .orElseGet(User::new);

        // 신규 유저일 때만 username 생성
        if (user.getUserId() == null) {
            String username = email.split("@")[0];
            if (username.length() > 20) {
                username = username.substring(0, 20);
            }
            user.setUsername(username);
        }

        user.setEmail(email);
        user.setName(name);
        user.setProvider("google");
        user.setProviderId(sub);
        user.setRole("USER");
        user.setStatus("ACTIVE");

        User savedUser = userRepository.save(user);

        System.out.println("==== SAVED USER ====");
        System.out.println("userId = " + savedUser.getUserId());
        System.out.println("email = " + savedUser.getEmail());

        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().write("로그인 성공: " + savedUser.getEmail());
    }
}