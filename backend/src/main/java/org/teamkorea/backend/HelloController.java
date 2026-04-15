package org.teamkorea.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
public class HelloController {

    @GetMapping("/api/hello")
    public String hello() {
        return "백엔드 연결 성공";
    }

     @GetMapping("/api/me")
    public String me(Authentication authentication) {
        if (authentication == null) {
            return "인증 정보 없음";
        }
        return "인증된 사용자: " + authentication.getName();
    }
}