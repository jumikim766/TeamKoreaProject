package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.Email;
import org.teamkorea.backend.domain.EmailAccount;
import org.teamkorea.backend.domain.EmailUrl;
import org.teamkorea.backend.domain.Url;
import org.teamkorea.backend.repository.EmailRepository;
import org.teamkorea.backend.repository.EmailUrlRepository;
import org.teamkorea.backend.repository.UrlRepository;

import java.net.IDN;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailSaveService {

    private final EmailRepository emailRepository;
    private final UrlRepository urlRepository;
    private final EmailUrlRepository emailUrlRepository;
    private final AnalysisService analysisService;

    private static final Set<String> TRACKING_PARAMS = Set.of(
            "gclid",
            "fbclid",
            "msclkid",
            "yclid",
            "mc_eid",
            "mc_cid",
            "igshid"
    );

    @Transactional
    public int saveEmailAndUrls(
            Long userId,
            EmailAccount account,
            String messageUid,
            String senderName,
            String senderEmail,
            String receiverEmail,
            String subject,
            String bodyText,
            String bodyHtml,
            LocalDateTime receivedAt,
            List<String> extractedUrls
    ) {
        if (emailRepository.existsByMessageUid(messageUid)) {
            return 0;
        }

        Email savedEmail = emailRepository.save(
                Email.builder()
                        .account(account)
                        .messageUid(messageUid)
                        .senderName(senderName)
                        .senderEmail(senderEmail)
                        .receiverEmail(receiverEmail)
                        .subject(subject)
                        .bodyHtml(bodyHtml)
                        .bodyText(bodyText)
                        .receivedAt(receivedAt)
                        .build()
        );

        int extractedUrlCount = 0;

        List<String> distinctUrls = extractedUrls;

        for (String rawUrl : distinctUrls) {

            String normalizedUrl = cleanUrl(rawUrl);

            if (normalizedUrl == null || normalizedUrl.isBlank()) {
                continue;
            }

            String urlHash = sha256(normalizedUrl);

String uniqueTestUrlHash = sha256(
        normalizedUrl + "|" + messageUid + "|" + extractedUrlCount
);

Url url = Url.builder()
        .normalizedUrl(normalizedUrl)
        .urlHash(uniqueTestUrlHash)
        .domain(extractDomain(normalizedUrl))
        .scheme(extractScheme(normalizedUrl))
        .firstSeenAt(LocalDateTime.now())
        .lastSeenAt(LocalDateTime.now())
        .seenCount(1)
        .build();

            Url savedUrl;

            try {
                savedUrl = urlRepository.saveAndFlush(url);
            } catch (DataIntegrityViolationException e) {
                savedUrl = urlRepository.findByUrlHash(urlHash)
                        .orElseThrow(() -> e);
            }

            emailUrlRepository.saveAndFlush(
                    EmailUrl.builder()
                            .email(savedEmail)
                            .url(savedUrl)
                            .rawUrl(rawUrl)
                            .linkText(null)
                            .build()
            );

            try {
                analysisService.analyzeWithLlmAndSave(
                        userId,
                        savedUrl.getUrlId(),
                        subject,
                        bodyText
                );
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("URL 분석 저장 실패: urlId=" + savedUrl.getUrlId()
                        + ", reason=" + e.getMessage());
            }

            extractedUrlCount++;
        }

        return extractedUrlCount;
    }

    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost() != null ? uri.getHost() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractScheme(String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme() != null ? uri.getScheme().toLowerCase() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String cleanUrl(String rawUrl) {

        if (rawUrl == null || rawUrl.isBlank()) {
            return null;
        }

        try {
            String cleaned = rawUrl.trim();

            cleaned = stripTrailingPunctuation(cleaned);

            URI uri = new URI(cleaned);

            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (scheme == null || host == null) {
                return null;
            }

            scheme = scheme.toLowerCase();

            if (!scheme.equals("http") && !scheme.equals("https")) {
                return null;
            }

            host = IDN.toASCII(host);

            if (host.startsWith("www.")) {
                host = host.substring(4);
            }

            int port = uri.getPort();

            if (scheme.equals("http") && port == 80) {
                port = -1;
            }

            if (scheme.equals("https") && port == 443) {
                port = -1;
            }

            String path = uri.getRawPath();

            if (path == null || path.isBlank() || path.equals("/")) {
                path = "";
            } else {
                path = path.replaceAll("/{2,}", "/");

                if (path.endsWith("/") && path.length() > 1) {
                    path = path.substring(0, path.length() - 1);
                }
            }

            String query = normalizeQuery(uri.getRawQuery());

            StringBuilder normalized = new StringBuilder();

            normalized.append(scheme)
                    .append("://")
                    .append(host);

            if (port != -1) {
                normalized.append(":").append(port);
            }

            normalized.append(path);

            if (query != null && !query.isBlank()) {
                normalized.append("?").append(query);
            }

            return normalized.toString();

        } catch (Exception e) {
            return null;
        }
    }

    private String stripTrailingPunctuation(String url) {

        if (url == null || url.isBlank()) {
            return url;
        }

        String cleaned = url;

        cleaned = cleaned.replaceAll("[\\]\\}\\>,\\.\"']+$", "");

        while (cleaned.endsWith(")") && countChar(cleaned, '(') < countChar(cleaned, ')')) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        return cleaned;
    }

    private int countChar(String text, char target) {

        int count = 0;

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == target) {
                count++;
            }
        }

        return count;
    }

    private String normalizeQuery(String rawQuery) {

        if (rawQuery == null || rawQuery.isBlank()) {
            return null;
        }

        String normalizedQuery = Arrays.stream(rawQuery.split("&"))
                .filter(param -> param != null && !param.isBlank())
                .map(param -> {
                    int eqIndex = param.indexOf("=");

                    String key = eqIndex < 0
                            ? param
                            : param.substring(0, eqIndex);

                    return new String[]{key, param};
                })
                .filter(pair -> !pair[0].toLowerCase().startsWith("utm_"))
                .filter(pair -> !TRACKING_PARAMS.contains(pair[0].toLowerCase()))
                .sorted(Comparator.comparing(pair -> pair[0]))
                .map(pair -> pair[1])
                .collect(Collectors.joining("&"));

        if (normalizedQuery.isBlank()) {
            return null;
        }

        return normalizedQuery;
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();

            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("해시 생성 실패", e);
        }
    }
}