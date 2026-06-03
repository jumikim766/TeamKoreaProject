package org.teamkorea.backend.ai;

import org.springframework.stereotype.Component;
import org.teamkorea.backend.ai.dto.UrlFeatureResult;

@Component
public class UrlFeatureExtractor {

    public UrlFeatureResult extract(String url, String domain) {
        System.out.println("URL = " + url);
System.out.println("DOMAIN = " + domain);
System.out.println("Suspicious TLD = " + hasSuspiciousTld(domain));

        if (url == null) {
            url = "";
        }

        if (domain == null) {
            domain = "";
        }

        int domainLength = domain.length();
        int hyphenCount = countChar(domain, '-');
        int dotCount = countChar(domain, '.');
        int digitCount = countDigits(domain);

        boolean hasIpAddress =
                url.matches(".*\\d+\\.\\d+\\.\\d+\\.\\d+.*");

        boolean hasPunycode =
                domain.contains("xn--");

        boolean hasSuspiciousKeyword =
                containsSuspiciousKeyword(url);

        boolean hasSuspiciousTld =
                hasSuspiciousTld(domain);

        double score = calculateScore(
                domainLength,
                hyphenCount,
                dotCount,
                digitCount,
                hasIpAddress,
                hasPunycode,
                hasSuspiciousKeyword,
                hasSuspiciousTld
        );

        return new UrlFeatureResult(
                domainLength,
                hyphenCount,
                dotCount,
                digitCount,
                hasIpAddress,
                hasPunycode,
                hasSuspiciousKeyword,
                hasSuspiciousTld,
                score
        );
    }

    private int countChar(String text, char target) {
        return (int) text.chars()
                .filter(ch -> ch == target)
                .count();
    }

    private int countDigits(String text) {
        return (int) text.chars()
                .filter(Character::isDigit)
                .count();
    }

    private boolean containsSuspiciousKeyword(String url) {
        String lower = url.toLowerCase();

        return lower.contains("login")
                || lower.contains("verify")
                || lower.contains("secure")
                || lower.contains("account")
                || lower.contains("update")
                || lower.contains("bank")
                || lower.contains("paypal")
                || lower.contains("confirm");
    }

    private boolean hasSuspiciousTld(String domain) {
    if (domain == null) {
        return false;
    }

    String lower = domain.toLowerCase().trim();

    return lower.equals("xyz")
            || lower.endsWith(".xyz")
            || lower.endsWith(".top")
            || lower.endsWith(".click")
            || lower.endsWith(".gq")
            || lower.endsWith(".ru");
}

    private double calculateScore(
            int domainLength,
            int hyphenCount,
            int dotCount,
            int digitCount,
            boolean hasIpAddress,
            boolean hasPunycode,
            boolean hasSuspiciousKeyword,
            boolean hasSuspiciousTld
    ) {
        double score = 0;

        if (domainLength >= 30) {
            score += 15;
        }

        if (hyphenCount >= 2) {
            score += 15;
        }

        if (dotCount >= 3) {
            score += 15;
        }

        if (digitCount >= 3) {
            score += 10;
        }

        if (hasIpAddress) {
            score += 30;
        }

        if (hasPunycode) {
            score += 25;
        }

        if (hasSuspiciousKeyword) {
            score += 20;
        }

        if (hasSuspiciousTld) {
            score += 20;
        }

        return Math.min(score, 100);
    }
}