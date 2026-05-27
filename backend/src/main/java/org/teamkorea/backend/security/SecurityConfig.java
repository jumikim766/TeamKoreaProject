package org.teamkorea.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.teamkorea.backend.service.CustomOAuth2UserService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.teamkorea.backend.dto.BaseResponse;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
public class SecurityConfig {

        private final OAuth2SuccessHandler oAuth2SuccessHandler;
        private final CustomOAuth2UserService customOAuth2UserService;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final ObjectMapper objectMapper;

        /**
         * CORS 허용 오리진을 application.properties에서 주입받아
         * 배포 환경마다 코드 수정 없이 변경할 수 있다.
         *
         * 예) app.cors.allowed-origins=https://example.com,https://www.example.com
         */
        @Value("${app.cors.allowed-origins}")
        private List<String> allowedOrigins;

        public SecurityConfig(OAuth2SuccessHandler oAuth2SuccessHandler,
                        CustomOAuth2UserService customOAuth2UserService,
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        ObjectMapper objectMapper) {
                this.oAuth2SuccessHandler = oAuth2SuccessHandler;
                this.customOAuth2UserService = customOAuth2UserService;
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.objectMapper = objectMapper;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers(
                                                                "/",
                                                                "/oauth2/**",
                                                                "/login/**",
                                                                "/error",
                                                                "/api/auth/signup",
                                                                "/api/auth/signup/send-code",
                                                                "/api/auth/signup/verify-code",
                                                                "/api/auth/login",
                                                                "/api/auth/reissue",
                                                                "/api/auth/logout",
                                                                "/api/hello",
                                                                "/api/users/check-username",
                                                                "/api/users/check-email",
                                                                "/api/auth/find-username/send-code",
                                                                "/api/auth/find-username/verify-code",
                                                                "/api/auth/password-reset/send-code",
                                                                "/api/auth/password-reset",
                                                                "/api/llm/test",
                                                                "/api/url-analysis/llm/url/**",
                                                                "/api/llm/url/**",
                                                                "/api/openai/**")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth -> oauth
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .successHandler(oAuth2SuccessHandler))
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                        response.setContentType("application/json;charset=UTF-8");
                                                        response.getWriter().write(
                                                                        objectMapper.writeValueAsString(
                                                                                        BaseResponse.error(
                                                                                                        "로그인이 필요합니다.",
                                                                                                        "UNAUTHORIZED")));
                                                })
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                                        response.setContentType("application/json;charset=UTF-8");
                                                        response.getWriter().write(
                                                                        objectMapper.writeValueAsString(
                                                                                        BaseResponse.error(
                                                                                                        "접근 권한이 없습니다.",
                                                                                                        "FORBIDDEN")));
                                                }))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(allowedOrigins);
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                config.setExposedHeaders(List.of("Authorization"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }
}