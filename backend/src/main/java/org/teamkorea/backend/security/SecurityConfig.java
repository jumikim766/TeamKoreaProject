package org.teamkorea.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.teamkorea.backend.service.CustomOAuth2UserService;

// 보안 규칙 정의
@Configuration
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler; // 로그인 성공 시 실행
    private final CustomOAuth2UserService customOAuth2UserService; // 유저 정보 가져오기

    public SecurityConfig(OAuth2SuccessHandler oAuth2SuccessHandler,
                          CustomOAuth2UserService customOAuth2UserService) {
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                // 나중에 JWT 쓸 거라 세션 안 씀
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/oauth2/**",
                                "/login/**",
                                "/error",

                                // 회원가입 / 로그인 허용
                                "/api/auth/signup",
                                "/api/auth/login",

                                // 테스트용 엔드포인트
                                "/api/hello",

                                // 이메일 계정 연동 테스트용 (나중에 삭제)
                                "/api/email-accounts",

                                // ===== 추가: Analysis 관련 API 테스트 허용 =====
                                "/api/url-analysis/**",      // 추가
                                "/api/analysis-history/**",  // 추가
                                "/api/reports/**",           // 추가
                                "/api/domain-reputation/**"  // 추가
                                // ============================================
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler) // 로그인 성공 시 이동
                );

        return http.build();
    }

    // 비밀번호 암호화용 (회원가입 필수)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}