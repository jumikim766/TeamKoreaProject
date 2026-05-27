package org.teamkorea.backend.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.ai.dto.LlmAnalysisResponse;
import org.teamkorea.backend.domain.Email;
import org.teamkorea.backend.domain.EmailAccount;
import org.teamkorea.backend.domain.EmailUrl;
import org.teamkorea.backend.domain.RiskLevel;
import org.teamkorea.backend.domain.Url;
import org.teamkorea.backend.domain.UrlAnalysis;
import org.teamkorea.backend.repository.EmailRepository;
import org.teamkorea.backend.repository.EmailUrlRepository;
import org.teamkorea.backend.repository.UrlRepository;
import org.teamkorea.backend.service.AnalysisService;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.net.IDN;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

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

    // URL 정규화 시 제거할 광고/추적 파라미터 목록
    private static final Set<String> TRACKING_PARAMS = Set.of(
            "gclid",
            "fbclid",
            "msclkid",
            "yclid",
            "mc_eid",
            "mc_cid",
            "igshid"
    );

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
            String bodyHtml,
            LocalDateTime receivedAt,
            List<String> extractedUrls) {
        // 1. emails 테이블에 이메일 저장
        Email savedEmail = emailRepository.save(
                Email.builder()
                        .account(account)
                        .messageUid(messageUid)
                        .senderName(senderName)
                        .senderEmail(senderEmail)
                        .receiverEmail(receiverEmail)
                        .subject(subject)
                        .bodyHtml(bodyHtml) // HTML 본문 원본 저장
                        .bodyText(bodyText)
                        .receivedAt(receivedAt)
                        .build());

        int extractedUrlCount = 0;

        // 같은 이메일 안에서 중복으로 추출된 URL 제거
        List<String> distinctUrls = extractedUrls.stream()
                .distinct()
                .toList();

        // 2. 이메일 본문에서 추출된 URL 목록 저장
        for (String rawUrl : distinctUrls) {

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
            // 같은 URL이 동시에 저장될 경우 unique 제약 조건 충돌이 날 수 있어서 대비
            Url savedUrl;

            try {
                savedUrl = urlRepository.save(url);
            } catch (DataIntegrityViolationException e) {
                // 이미 다른 동기화 작업에서 같은 URL을 먼저 저장한 경우 다시 조회
                savedUrl = urlRepository.findByUrlHash(urlHash)
                        .orElseThrow(() -> e);
            }

            // 5. email_urls 테이블에 이메일-URL 연결 저장
            emailUrlRepository.save(
                    EmailUrl.builder()
                            .email(savedEmail)
                            .url(savedUrl)
                            .rawUrl(rawUrl)
                            .linkText(null)
                            .build());

           try {
    analysisService.analyzeWithLlmAndSave(
            userId,
            savedUrl.getUrlId(),
            subject,
            bodyText
    );
} catch (Exception e) {
    System.out.println("[FINAL ANALYSIS ERROR] urlId = " + savedUrl.getUrlId());
}
            // 실제 저장/연결 처리된 URL 개수 증가
            extractedUrlCount++;
        }

        // 실제 저장/연결 처리된 URL 개수 반환
        return extractedUrlCount;
    }
   
  
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
            // 앞뒤 공백 제거
            String cleaned = rawUrl.trim();

            // 이메일 본문에서 URL 뒤에 붙은 불필요한 문장부호 제거
            cleaned = stripTrailingPunctuation(cleaned);

            URI uri = new URI(cleaned);

            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (scheme == null || host == null) {
                return null;
            }

            // http, https만 저장
            scheme = scheme.toLowerCase();

            if (!scheme.equals("http") && !scheme.equals("https")) {
                return null;
            }

            // 도메인 소문자 변환 + 한글 도메인 대응
            host = IDN.toASCII(host.toLowerCase());

            // www. 제거
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }

            // 기본 포트 제거
            int port = uri.getPort();

            if (scheme.equals("http") && port == 80) {
                port = -1;
            }

            if (scheme.equals("https") && port == 443) {
                port = -1;
            }

            // path 정리
            String path = uri.getRawPath();

            if (path == null || path.isBlank() || path.equals("/")) {
                path = "";
            } else {
                // 연속 슬래시 정리
                path = path.replaceAll("/{2,}", "/");

                // 경로 끝의 / 제거
                if (path.endsWith("/") && path.length() > 1) {
                    path = path.substring(0, path.length() - 1);
                }
            }

            // query 정리: 트래킹 파라미터 제거 + 파라미터 정렬
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

            // fragment(#...)는 중복 방지를 위해 저장하지 않음
            String result = normalized.toString();

            /*
            // 너무 긴 URL은 저장하지 않음
            if (result.length() > 8192) {
                return null;
            }*/

            return result;

        } catch (Exception e) {
            return null;
        }
    }

        // URL 끝에 붙은 불필요한 문장부호 제거
    private String stripTrailingPunctuation(String url) {

        if (url == null || url.isBlank()) {
            return url;
        }

        String cleaned = url;

        // 쉼표, 마침표, 따옴표, 대괄호, 중괄호 등 제거
        cleaned = cleaned.replaceAll("[\\]\\}\\>,\\.\"']+$", "");

        // 닫는 괄호는 여는 괄호보다 많을 때만 제거
        // 예: https://test.com) -> 제거
        // 예: https://en.wikipedia.org/wiki/Foo_(bar) -> 유지
        while (cleaned.endsWith(")") && countChar(cleaned, '(') < countChar(cleaned, ')')) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        return cleaned;
    }

    // 특정 문자 개수 세기
    private int countChar(String text, char target) {

        int count = 0;

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == target) {
                count++;
            }
        }

        return count;
    }

    // query 파라미터 정규화
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
                // utm_ 계열 광고 파라미터 제거
                .filter(pair -> !pair[0].toLowerCase().startsWith("utm_"))
                // gclid, fbclid 등 추적 파라미터 제거
                .filter(pair -> !TRACKING_PARAMS.contains(pair[0].toLowerCase()))
                // 파라미터 키 기준 정렬
                .sorted(Comparator.comparing(pair -> pair[0]))
                .map(pair -> pair[1])
                .collect(Collectors.joining("&"));

        if (normalizedQuery.isBlank()) {
            return null;
        }

        return normalizedQuery;
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