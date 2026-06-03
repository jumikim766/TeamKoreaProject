package org.teamkorea.backend.ai;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class WhitelistService {

    private static final Set<String> WHITELIST = Set.of(
            "google.com",
            "naver.com",
            "kakao.com",
            "daum.net",
            "github.com",
            "openai.com",
            "microsoft.com",
            "apple.com",
            "youtube.com"
    );

    public boolean isWhitelisted(String domain) {
        if (domain == null || domain.isBlank()) {
            return false;
        }

        String lower = domain.toLowerCase();

        return WHITELIST.stream()
                .anyMatch(allowed ->
                        lower.equals(allowed)
                                || lower.endsWith("." + allowed)
                );
    }
}