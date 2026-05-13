package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.Email;
import org.teamkorea.backend.domain.EmailAccount;
import org.teamkorea.backend.domain.EmailUrl;
import org.teamkorea.backend.domain.Url;
import org.teamkorea.backend.repository.EmailRepository;
import org.teamkorea.backend.repository.EmailUrlRepository;
import org.teamkorea.backend.repository.UrlRepository;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailSaveService {

    private final EmailRepository emailRepository;
    private final UrlRepository urlRepository;
    private final EmailUrlRepository emailUrlRepository;
    private final AnalysisService analysisService;

    
    // 이메일 1개 저장 + 해당 이메일에서 추출된 URL 저장
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
            LocalDateTime receivedAt,
            List<String> extractedUrls
    ) {
        // 1. emails 테이블에 이메일 저장
        Email savedEmail = emailRepository.save(
                Email.builder()
                        .account(account)
                        .messageUid(messageUid)
                        .senderName(senderName)
                        .senderEmail(senderEmail)
                        .receiverEmail(receiverEmail)
                        .subject(subject)
                        .bodyText(bodyText)
                        .receivedAt(receivedAt)
                        .build()
        );

        int extractedUrlCount = 0;

        // 2. 이메일 본문에서 추출된 URL 목록 저장
        for (String rawUrl : extractedUrls) {

            // 원본 URL을 정규화
            String normalizedUrl = cleanUrl(rawUrl);

            // 정규화 실패한 URL은 저장하지 않음
            if (normalizedUrl == null || normalizedUrl.isBlank()) {
                continue;
            }

            // 정규화된 URL 기준으로 해시 생성
            String urlHash = sha256(normalizedUrl);

            Url url;

            // 3. 이미 저장된 URL인지 확인
            Optional<Url> existingUrlOpt = urlRepository.findByUrlHash(urlHash);

            if (existingUrlOpt.isPresent()) {
                // 기존 URL이면 lastSeenAt, seenCount 갱신
                url = existingUrlOpt.get();
                url.setLastSeenAt(LocalDateTime.now());
                url.setSeenCount(url.getSeenCount() + 1);
            } else {
                // 신규 URL이면 urls 테이블에 새로 저장할 객체 생성
                url = Url.builder()
                        .normalizedUrl(normalizedUrl)
                        .urlHash(urlHash)
                        .domain(extractDomain(normalizedUrl))
                        .scheme(extractScheme(normalizedUrl))
                        .firstSeenAt(LocalDateTime.now())
                        .lastSeenAt(LocalDateTime.now())
                        .seenCount(1)
                        .build();
            }

            // 4. urls 테이블 저장
            Url savedUrl = urlRepository.save(url);

            // 5. email_urls 테이블에 이메일-URL 연결 저장
            emailUrlRepository.save(
                    EmailUrl.builder()
                            .email(savedEmail)
                            .url(savedUrl)
                            .rawUrl(rawUrl)
                            .linkText(null)
                            .build()
            );

            // 6. URL 분석 결과 저장
            analysisService.analyzeAndSave(userId, savedUrl.getUrlId());

            extractedUrlCount++;
        }

        // 실제 저장/연결 처리된 URL 개수 반환
        return extractedUrlCount;
    }

    // URL에서 도메인 추출
    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost() != null ? uri.getHost().toLowerCase() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // URL에서 프로토콜 추출
    private String extractScheme(String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme() != null ? uri.getScheme().toLowerCase() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // URL 정규화
    private String cleanUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return null;
        }

        try {
            String cleaned = rawUrl.trim();

            // 이메일/HTML에서 URL 뒤에 붙는 닫는 문자 제거
            cleaned = cleaned.replaceAll("[\\)\\]\\}\\>,\\.\"']+$", "");

            URI uri = new URI(cleaned);

            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (scheme == null || host == null) {
                return null;
            }

            scheme = scheme.toLowerCase();
            host = host.toLowerCase();

            // http, https만 저장
            if (!scheme.equals("http") && !scheme.equals("https")) {
                return null;
            }

            String path = uri.getRawPath();
            String query = uri.getRawQuery();

            if (path == null || path.isBlank()) {
                path = "";
            }

            // 마지막 / 제거
            if (path.equals("/")) {
                path = "";
            }

            StringBuilder normalized = new StringBuilder();
            normalized.append(scheme)
                    .append("://")
                    .append(host);

            if (uri.getPort() != -1) {
                normalized.append(":").append(uri.getPort());
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

    // URL 중복 체크용 SHA-256 해시 생성
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