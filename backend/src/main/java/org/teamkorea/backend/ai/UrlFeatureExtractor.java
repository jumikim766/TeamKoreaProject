package org.teamkorea.backend.ai;

import org.springframework.stereotype.Component;
import org.teamkorea.backend.ai.dto.UrlFeatureResult;

@Component
public class UrlFeatureExtractor {

    public UrlFeatureResult extract(String url, String domain) {
        System.out.println("URL = " + url);
System.out.println("DOMAIN = " + domain);
System.out.println("Suspicious TLD = " + hasSuspiciousTld(domain));
boolean hasBrandImpersonation =
        containsBrandImpersonation(domain);
        if (url == null) {
            url = "";
        }

        if (domain == null) {
            domain = "";
        }
        boolean hasSubdomainBrandImpersonation =
        containsSubdomainBrandImpersonation(domain);

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
        boolean hasShortUrlService =
        hasShortUrlService(url, domain);
        
        double score = calculateScore(
        domainLength,
        hyphenCount,
        dotCount,
        digitCount,
        hasIpAddress,
        hasPunycode,
        hasSuspiciousKeyword,
        hasSuspiciousTld,
        hasBrandImpersonation,
        hasSubdomainBrandImpersonation,
        hasShortUrlService
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
        hasBrandImpersonation,
        hasSubdomainBrandImpersonation,
        hasShortUrlService,
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
    private boolean containsBrandImpersonation(String domain) {

    if (domain == null) {
        return false;
    }

    String lower = domain.toLowerCase();

    boolean officialDomain =
            lower.equals("google.com") || lower.endsWith(".google.com")
            || lower.equals("naver.com") || lower.endsWith(".naver.com")
            || lower.equals("kakao.com") || lower.endsWith(".kakao.com")
            || lower.equals("paypal.com") || lower.endsWith(".paypal.com")
            || lower.equals("microsoft.com") || lower.endsWith(".microsoft.com")
            || lower.equals("apple.com") || lower.endsWith(".apple.com")
            || lower.equals("amazon.com") || lower.endsWith(".amazon.com");

    if (officialDomain) {
        return false;
    }

    return lower.contains("google")
            || lower.contains("g00gle")
            || lower.contains("goog1e")
            || lower.contains("gooogle")
            || lower.contains("naver")
            || lower.contains("n@ver")
            || lower.contains("kakao")
            || lower.contains("paypal")
            || lower.contains("paypa1")
            || lower.contains("paypai")
            || lower.contains("microsoft")
            || lower.contains("micros0ft")
            || lower.contains("rnicrosoft")
            || lower.contains("apple")
            || lower.contains("app1e")
            || lower.contains("amazon")
            || lower.contains("amaz0n");
}

private boolean containsSubdomainBrandImpersonation(String domain) {
    if (domain == null || domain.isBlank()) {
        return false;
    }

    String lower = domain.toLowerCase();

    boolean officialDomain =
            lower.equals("google.com") || lower.endsWith(".google.com")
            || lower.equals("naver.com") || lower.endsWith(".naver.com")
            || lower.equals("kakao.com") || lower.endsWith(".kakao.com")
            || lower.equals("paypal.com") || lower.endsWith(".paypal.com")
            || lower.equals("microsoft.com") || lower.endsWith(".microsoft.com")
            || lower.equals("apple.com") || lower.endsWith(".apple.com")
            || lower.equals("amazon.com") || lower.endsWith(".amazon.com");

    if (officialDomain) {
        return false;
    }

    String[] parts = lower.split("\\.");

    if (parts.length < 3) {
        return false;
    }

    for (int i = 0; i < parts.length - 2; i++) {
        String sub = parts[i];

        if (sub.equals("google")
                || sub.equals("naver")
                || sub.equals("kakao")
                || sub.equals("paypal")
                || sub.equals("microsoft")
                || sub.equals("apple")
                || sub.equals("amazon")) {
            return true;
        }
    }

    return lower.contains("google.login")
            || lower.contains("paypal.verify")
            || lower.contains("naver.login")
            || lower.contains("kakao.login")
            || lower.contains("microsoft.login");
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
private boolean hasShortUrlService(String url, String domain) {
    if (url == null) {
        url = "";
    }

    if (domain == null) {
        domain = "";
    }

    String lowerUrl = url.toLowerCase();
    String lowerDomain = domain.toLowerCase();

    return lowerUrl.contains("bit.ly")
            || lowerUrl.contains("tinyurl.com")
            || lowerUrl.contains("t.co")
            || lowerUrl.contains("cutt.ly")
            || lowerUrl.contains("is.gd")
            || lowerUrl.contains("ow.ly")
            || lowerUrl.contains("rebrand.ly")
            || lowerUrl.contains("shorturl.at")
            || lowerUrl.contains("url.kr")
            || lowerUrl.contains("vo.la")
            || lowerUrl.contains("han.gl")
            || lowerUrl.contains("me2.kr")
            || lowerDomain.equals("bit.ly")
            || lowerDomain.equals("tinyurl.com")
            || lowerDomain.equals("t.co")
            || lowerDomain.equals("cutt.ly")
            || lowerDomain.equals("is.gd")
            || lowerDomain.equals("ow.ly")
            || lowerDomain.equals("rebrand.ly")
            || lowerDomain.equals("shorturl.at")
            || lowerDomain.equals("url.kr")
            || lowerDomain.equals("vo.la")
            || lowerDomain.equals("han.gl")
            || lowerDomain.equals("me2.kr");
}

    private double calculateScore(
            int domainLength,
            int hyphenCount,
            int dotCount,
            int digitCount,
            boolean hasIpAddress,
            boolean hasPunycode,
            boolean hasSuspiciousKeyword,
            boolean hasSuspiciousTld,
            boolean hasBrandImpersonation,
            boolean hasSubdomainBrandImpersonation,
            boolean hasShortUrlService
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
        if (hasShortUrlService) {
    score += 20;
}
        if (hasBrandImpersonation) {
    score += 50;
}

if (hasBrandImpersonation && hasSuspiciousKeyword) {
    score += 20;
}
    if (hasSubdomainBrandImpersonation) {
    score += 35;
}

if (hasSubdomainBrandImpersonation && hasSuspiciousKeyword) {
    score += 20;
}
if (hasShortUrlService && hasBrandImpersonation) {
    score += 15;
}
return Math.min(score, 100);
    }
}