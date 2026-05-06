package org.teamkorea.backend.service;

import jakarta.mail.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.*;
import org.teamkorea.backend.dto.EmailAccountRequestDto;
import org.teamkorea.backend.dto.EmailAccountResponseDto;
import org.teamkorea.backend.repository.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailAccountService {

    private final EmailAccountRepository emailAccountRepository;
    private final UserRepository userRepository;
    private final EmailRepository emailRepository;
    private final UrlRepository urlRepository;
    private final EmailUrlRepository emailUrlRepository;
    private final AnalysisService analysisService;
    
    // 이메일 계정 등록
    @Transactional
    public EmailAccountResponseDto createEmailAccount(Long userId, EmailAccountRequestDto request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (emailAccountRepository.existsByUserAndEmail(user, request.getEmail())) {
            throw new IllegalStateException("이미 등록된 이메일 계정입니다.");
        }

        EmailProvider provider = parseProvider(request.getProvider());

        ImapConfig imapConfig = resolveImapConfig(provider, request);

        EmailAccount emailAccount = EmailAccount.builder()
                .user(user)
                .email(request.getEmail())
                .provider(provider.name())
                .imapHost(imapConfig.host())
                .imapPort(imapConfig.port())
                .loginId(request.getLoginId())
                .secretEnc(request.getSecret().getBytes(StandardCharsets.UTF_8))
                .active(true)
                .lastSyncStatus(null)
                .lastSyncedAt(null)
                .build();

        EmailAccount saved = emailAccountRepository.save(emailAccount);

        return toResponse(saved);
    }

    // 이메일 계정 목록 조회
    @Transactional(readOnly = true)
    public List<EmailAccountResponseDto> getEmailAccounts(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return emailAccountRepository.findAllByUser(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 이메일 계정 삭제
    @Transactional
    public void deleteEmailAccount(Long userId, Long accountId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        EmailAccount emailAccount = emailAccountRepository.findByAccountIdAndUser(accountId, user)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 이메일 계정을 찾을 수 없습니다."));

        emailAccountRepository.delete(emailAccount);
    }

    // 이메일 동기화
    @Transactional
    public Map<String, Object> syncEmails(Long userId, Long accountId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        EmailAccount account = emailAccountRepository.findByAccountIdAndUser(accountId, user)
                .orElseThrow(() -> new IllegalArgumentException("이메일 계정을 찾을 수 없습니다."));

        if (Boolean.FALSE.equals(account.getActive())) {
            throw new IllegalStateException("비활성화된 이메일 계정입니다.");
        }

        Store store = null;
        Folder inbox = null;

        int collectedEmailCount = 0;
        int savedEmailCount = 0;
        int skippedEmailCount = 0;
        int extractedUrlCount = 0;

        try {
            System.out.println("========== SYNC START ==========");
            System.out.println("accountId = " + account.getAccountId());
            System.out.println("provider = " + account.getProvider());
            System.out.println("email = " + account.getEmail());
            System.out.println("loginId = " + account.getLoginId());
            System.out.println("imapHost = " + account.getImapHost());
            System.out.println("imapPort = " + account.getImapPort());

            // 마지막 동기화 시각 조회
            LocalDateTime lastSyncedAt = account.getLastSyncedAt();

            Properties props = new Properties();
            props.put("mail.store.protocol", "imap");
            props.put("mail.imap.host", account.getImapHost());
            props.put("mail.imap.port", String.valueOf(account.getImapPort()));
            props.put("mail.imap.ssl.enable", "true");
            props.put("mail.imap.ssl.trust", "*");

            Session session = Session.getInstance(props);
            store = session.getStore("imap");

            String secret = new String(account.getSecretEnc(), StandardCharsets.UTF_8);

            store.connect(
                    account.getImapHost(),
                    account.getLoginId(),
                    secret
            );

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();

            int startIndex = messages.length - 1;
            int endIndex = Math.max(0, messages.length - 10);

            for (int i = startIndex; i >= endIndex; i--) {

                Message msg = messages[i];

                collectedEmailCount++;

                LocalDateTime receivedAt = msg.getReceivedDate() != null
                        ? LocalDateTime.ofInstant(
                        msg.getReceivedDate().toInstant(),
                        ZoneId.systemDefault()
                )
                        : LocalDateTime.now();

                /*
                if (lastSyncedAt != null && !receivedAt.isAfter(lastSyncedAt)) {
                    skippedEmailCount++;
                    continue;
                }
*/
                String messageUid = buildMessageUid(msg);

                /* 
                if (emailRepository.existsByMessageUid(messageUid)) {
                    skippedEmailCount++;
                    continue;
                }*/

                String subject = msg.getSubject();
                String bodyText = getText(msg);
                System.out.println("메일 제목: " + subject);
System.out.println("메일 내용: " + bodyText);

                Email savedEmail = emailRepository.save(
                        Email.builder()
                                .account(account)
                                .messageUid(messageUid)
                                .senderName(null)
                                .senderEmail(extractSenderEmail(msg))
                                .receiverEmail(account.getEmail())
                                .subject(subject)
                                .bodyText(bodyText)
                                .receivedAt(receivedAt)
                                .build()
                );

                savedEmailCount++;

                List<String> extractedUrls = extractUrls(bodyText);
                    System.out.println("추출된 URL 개수: " + extractedUrls.size());
                    System.out.println("추출된 URL 리스트: " + extractedUrls);
                for (String rawUrl : extractedUrls) {

    String normalizedUrl = cleanUrl(rawUrl);

    if (normalizedUrl == null || normalizedUrl.isBlank()) {
        continue;
    }

    String urlHash = sha256(normalizedUrl);

                // URL 중복 처리 (이미 존재하면 seenCount 증가, lastSeenAt 갱신)
                Url url;

                Optional<Url> existingUrlOpt = urlRepository.findByUrlHash(urlHash);

                if (existingUrlOpt.isPresent()) {
                    url = existingUrlOpt.get();
                    url.setLastSeenAt(LocalDateTime.now());
                    url.setSeenCount(url.getSeenCount() + 1);
                } else {
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

                // 기존 URL이든 신규 URL이든 저장
                Url savedUrl = urlRepository.save(url);

                emailUrlRepository.save(
                    EmailUrl.builder()
                        .email(savedEmail)
                        .url(savedUrl)
                        .rawUrl(normalizedUrl)
                        .linkText(null)
                        .build()
                );

                analysisService.analyzeAndSave(userId, savedUrl.getUrlId());

                extractedUrlCount++;
                }
            }

            account.updateSyncSuccess();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("accountId", account.getAccountId());
            result.put("collectedEmailCount", collectedEmailCount);
            result.put("savedEmailCount", savedEmailCount);
            result.put("skippedEmailCount", skippedEmailCount);
            result.put("extractedUrlCount", extractedUrlCount);
            result.put("lastSyncedAt", account.getLastSyncedAt());

            System.out.println("========== SYNC SUCCESS ==========");

            return result;

        } catch (Exception e) {
            account.updateSyncFailed();

            System.out.println("========== SYNC ERROR ==========");
            e.printStackTrace();
            System.out.println("========== SYNC ERROR END ==========");

            throw new RuntimeException("이메일 동기화 중 오류가 발생했습니다.", e);

        } finally {
            try {
                if (inbox != null && inbox.isOpen()) {
                    inbox.close(false);
                }

                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (MessagingException ignored) {
            }
        }
    }

    // provider 문자열을 enum으로 변환
    private EmailProvider parseProvider(String provider) {
        try {
            return EmailProvider.valueOf(provider.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("지원하지 않는 provider입니다.");
        }
    }

    // provider별 IMAP 설정 분기
    private ImapConfig resolveImapConfig(EmailProvider provider, EmailAccountRequestDto request) {

        switch (provider) {
            case GMAIL:
                return new ImapConfig("imap.gmail.com", 993);

            case NAVER:
                return new ImapConfig("imap.naver.com", 993);

            case DAUM:
                return new ImapConfig("imap.daum.net", 993);

            case OUTLOOK:
                return new ImapConfig("outlook.office365.com", 993);

            case CUSTOM:
                if (request.getImapHost() == null || request.getImapHost().isBlank()) {
                    throw new IllegalArgumentException("CUSTOM provider는 imapHost가 필요합니다.");
                }

                if (request.getImapPort() == null) {
                    throw new IllegalArgumentException("CUSTOM provider는 imapPort가 필요합니다.");
                }

                return new ImapConfig(request.getImapHost(), request.getImapPort());

            default:
                throw new IllegalArgumentException("지원하지 않는 provider입니다.");
        }
    }

    // 응답 DTO 변환
    private EmailAccountResponseDto toResponse(EmailAccount emailAccount) {
        return EmailAccountResponseDto.builder()
                .accountId(emailAccount.getAccountId())
                .userId(emailAccount.getUser().getUserId())
                .provider(emailAccount.getProvider())
                .email(emailAccount.getEmail())
                .active(emailAccount.getActive())
                .lastSyncStatus(emailAccount.getLastSyncStatus())
                .lastSyncedAt(emailAccount.getLastSyncedAt())
                .createdAt(emailAccount.getCreatedAt())
                .build();
    }

    // 이메일 본문 추출
    private String getText(Part part) throws Exception {

        if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
            Object content = part.getContent();
            return content != null ? content.toString() : "";
        }

        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();

            for (int i = 0; i < multipart.getCount(); i++) {
                String text = getText(multipart.getBodyPart(i));

                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        }

        return "";
    }

    // 본문에서 URL 추출
    private List<String> extractUrls(String text) {

        Pattern pattern = Pattern.compile("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+");
        Matcher matcher = pattern.matcher(text == null ? "" : text);

        return matcher.results()
                .map(match -> match.group())
                .toList();
    }

    // 발신자 이메일 추출
    private String extractSenderEmail(Message msg) {
        try {
            Address[] froms = msg.getFrom();

            if (froms == null || froms.length == 0) {
                return null;
            }

            return froms[0].toString();

        } catch (Exception e) {
            return null;
        }
    }

    // 도메인 추출
    private String extractDomain(String url) {
        try {
            return url.split("/")[2];
        } catch (Exception e) {
            return null;
        }
    }

    // 프로토콜 추출
    private String extractScheme(String url) {
        try {
            return url.split(":")[0];
        } catch (Exception e) {
            return null;
        }
    }
    private String cleanUrl(String rawUrl) {
    if (rawUrl == null) {
        return null;
    }

    return rawUrl
            .trim()
            .replaceAll("[\\)\\]\\}\\>,\\.]+$", "");
}
    // url 해시 생성
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

    // 이메일 중복 방지용 ID 생성
    private String buildMessageUid(Message msg) throws Exception {

        String[] messageIds = msg.getHeader("Message-ID");

        if (messageIds != null && messageIds.length > 0 && messageIds[0] != null && !messageIds[0].isBlank()) {
            return sha256(messageIds[0]);
        }

        String subject = msg.getSubject() != null ? msg.getSubject() : "";

        String sentDate = msg.getSentDate() != null ? msg.getSentDate().toInstant().toString() : "";

        String from = (msg.getFrom() != null && msg.getFrom().length > 0)
                ? msg.getFrom()[0].toString()
                : "";

        String receivedDate = msg.getReceivedDate() != null
                ? msg.getReceivedDate().toInstant().toString()
                : "";

        return sha256(subject + "|" + sentDate + "|" + receivedDate + "|" + from);
    }
}