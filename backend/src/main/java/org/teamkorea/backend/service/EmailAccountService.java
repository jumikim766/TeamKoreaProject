package org.teamkorea.backend.service;

import jakarta.mail.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.Email;
import org.teamkorea.backend.domain.EmailAccount;
import org.teamkorea.backend.domain.EmailUrl;
import org.teamkorea.backend.domain.Url;
import org.teamkorea.backend.domain.User;
import org.teamkorea.backend.dto.EmailAccountRequestDto;
import org.teamkorea.backend.dto.EmailAccountResponse;
import org.teamkorea.backend.repository.EmailAccountRepository;
import org.teamkorea.backend.repository.EmailRepository;
import org.teamkorea.backend.repository.EmailUrlRepository;
import org.teamkorea.backend.repository.UrlRepository;
import org.teamkorea.backend.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailAccountService {

    private final EmailAccountRepository emailAccountRepository;
    private final UserRepository userRepository;
    private final EmailRepository emailRepository;
    private final UrlRepository urlRepository;
    private final EmailUrlRepository emailUrlRepository;

    @Transactional
    public EmailAccountResponse createEmailAccount(Long userId, EmailAccountRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (emailAccountRepository.existsByUserAndEmail(user, request.getEmail())) {
            throw new IllegalStateException("이미 등록된 이메일 계정입니다.");
        }

        EmailAccount emailAccount = EmailAccount.builder()
                .user(user)
                .email(request.getEmail())
                .provider(request.getProvider())
                .imapHost(request.getImapHost())
                .imapPort(request.getImapPort())
                .loginId(request.getLoginId())

                // TODO: 현재는 임시 byte[] 변환입니다.
                // 최종적으로는 AES 등 암호화 후 secretEnc에 저장해야 합니다.
                .secretEnc(request.getPassword().getBytes(StandardCharsets.UTF_8))

                .active(true)
                .lastSyncStatus(null)
                .lastSyncedAt(null)
                .build();

        EmailAccount saved = emailAccountRepository.save(emailAccount);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EmailAccountResponse> getEmailAccounts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return emailAccountRepository.findAllByUser(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteEmailAccount(Long userId, Long accountId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        EmailAccount emailAccount = emailAccountRepository.findByAccountIdAndUser(accountId, user)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 이메일 계정을 찾을 수 없습니다."));

        emailAccountRepository.delete(emailAccount);
    }

    @Transactional
    public void syncEmails(Long userId, Long accountId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        EmailAccount account = emailAccountRepository.findByAccountIdAndUser(accountId, user)
                .orElseThrow(() -> new IllegalArgumentException("이메일 계정을 찾을 수 없습니다."));

        if (Boolean.FALSE.equals(account.getActive())) {
            throw new IllegalStateException("비활성화된 이메일 계정입니다.");
        }

        Store store = null;
        Folder inbox = null;

        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imap");
            props.put("mail.imap.host", account.getImapHost());
            props.put("mail.imap.port", String.valueOf(account.getImapPort()));
            props.put("mail.imap.ssl.enable", "true");

            Session session = Session.getInstance(props);
            store = session.getStore("imap");

            // TODO: secretEnc 복호화 로직 적용 필요
            // 현재는 임시로 byte[]를 문자열로 되돌려 사용합니다.
            String password = new String(account.getSecretEnc(), StandardCharsets.UTF_8);

            store.connect(
                    account.getImapHost(),
                    account.getLoginId(),
                    password
            );

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();

            for (int i = messages.length - 1; i >= Math.max(0, messages.length - 10); i--) {
                Message msg = messages[i];

                String messageUid = buildMessageUid(msg);

                if (emailRepository.existsByMessageUid(messageUid)) {
                    continue;
                }

                String subject = msg.getSubject();
                String bodyText = getText(msg);

                Email savedEmail = emailRepository.save(
                        Email.builder()
                                .account(account)
                                .messageUid(messageUid)
                                .subject(subject)
                                .bodyText(bodyText)
                                .receivedAt(
                                        msg.getReceivedDate() != null
                                                ? LocalDateTime.ofInstant(
                                                        msg.getReceivedDate().toInstant(),
                                                        ZoneId.systemDefault()
                                                )
                                                : LocalDateTime.now()
                                )
                                .build()
                );

                List<String> extractedUrls = extractUrls(bodyText);

                for (String rawUrl : extractedUrls) {
                    String normalizedUrl = rawUrl.trim();
                    String urlHash = sha256(normalizedUrl);

                    Url url = urlRepository.findByUrlHash(urlHash)
                            .orElseGet(() -> urlRepository.save(
                                    Url.builder()
                                            .normalizedUrl(normalizedUrl)
                                            .urlHash(urlHash)
                                            .domain(extractDomain(normalizedUrl))
                                            .scheme(extractScheme(normalizedUrl))
                                            .firstSeenAt(LocalDateTime.now())
                                            .lastSeenAt(LocalDateTime.now())
                                            .seenCount(1)
                                            .build()
                            ));

                    // TODO: Url 엔티티에 updateSeen() 같은 메서드가 있다면 여기서 호출 권장
                    // 기존 URL이 다시 발견된 경우 lastSeenAt, seenCount 갱신 필요

                    emailUrlRepository.save(
                            EmailUrl.builder()
                                    .email(savedEmail)
                                    .url(url)
                                    .rawUrl(rawUrl)
                                    .build()
                    );
                }
            }

            // EmailAccount 엔티티에 updateSyncSuccess() 메서드가 있어야 합니다.
            account.updateSyncSuccess();

        } catch (Exception e) {
            // EmailAccount 엔티티에 updateSyncFailed() 메서드가 있어야 합니다.
            account.updateSyncFailed();
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
                // 종료 중 예외는 동기화 본문 예외보다 중요도가 낮아서 무시
            }
        }
    }

    private EmailAccountResponse toResponse(EmailAccount emailAccount) {
        return EmailAccountResponse.builder()
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

    private List<String> extractUrls(String text) {
        Pattern pattern = Pattern.compile("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+");
        Matcher matcher = pattern.matcher(text == null ? "" : text);

        return matcher.results()
                .map(match -> match.group())
                .toList();
    }

    private String extractDomain(String url) {
        try {
            return url.split("/")[2];
        } catch (Exception e) {
            return null;
        }
    }

    private String extractScheme(String url) {
        try {
            return url.split(":")[0];
        } catch (Exception e) {
            return null;
        }
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

    private String buildMessageUid(Message msg) throws Exception {
        String subject = msg.getSubject() != null ? msg.getSubject() : "";
        String sentDate = msg.getSentDate() != null
                ? msg.getSentDate().toInstant().toString()
                : "";
        String from = (msg.getFrom() != null && msg.getFrom().length > 0)
                ? msg.getFrom()[0].toString()
                : "";

        return sha256(subject + "|" + sentDate + "|" + from);
    }
}