package org.teamkorea.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.teamkorea.backend.service.CustomOAuth2UserService;

import jakarta.servlet.http.HttpServletResponse;

// 보안 규칙 정의
@Configuration
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler; // 소셜 로그인 성공 시 실행
    private final CustomOAuth2UserService customOAuth2UserService; // 소셜 로그인 유저 정보 처리
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // JWT 인증 필터


    public SecurityConfig(OAuth2SuccessHandler oAuth2SuccessHandler,
                          CustomOAuth2UserService customOAuth2UserService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                // JWT 기반 인증이므로 서버 세션 사용 안 함
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/oauth2/**",
                                "/login/**",
                                "/error",

                                // 회원가입 / 일반 로그인은 토큰 없이 접근 허용
                                "/api/auth/signup",
                                "/api/auth/login",

                                // Access Token 재발급은 Refresh Token 기반이므로 접근 허용
                                "/api/auth/reissue",

                                // 테스트용 엔드포인트(회원가입/로그인/JWT 테스트 끝나면 삭제)
                                "/api/hello"

                                // 이메일 계정 연동 테스트용 (나중에 삭제)
                                // "/api/email-accounts",

                                // ===== 추가: Analysis 관련 API 테스트 허용(나중에 정책 재정리 필요)
                                // "/api/url-analysis/**",
                                // "/api/analysis-history/**",
                                // "/api/reports/**",
                                // "/api/domain-reputation/**"
                        ).permitAll()
                        .anyRequest().authenticated()// 위에서 허용하지 않은 모든 요청은 JWT 인증 필요
                )
                 .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )

                // 인증 안 된 요청은 401 JSON 응답
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                )
                // Spring Security 기본 인증 필터 전에 JWT 필터 실행
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // 비밀번호 암호화용 (회원가입 필수)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}