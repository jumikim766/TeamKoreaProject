package org.teamkorea.backend.service;

import jakarta.mail.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.*;
import org.teamkorea.backend.dto.EmailAccountRequestDto;
import org.teamkorea.backend.dto.EmailAccountResponseDto;
import org.teamkorea.backend.repository.*;
import org.teamkorea.backend.security.CryptoUtil;
import org.teamkorea.backend.exception.BusinessException;
import org.teamkorea.backend.exception.ErrorCode;

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

import jakarta.mail.internet.InternetAddress;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailAccountService {

    // 최초 동기화 때 가져올 메일 개수
    private static final int FIRST_SYNC_LIMIT = 100;

    // 이후 동기화 때 확인할 최신 메일 개수
    // private static final int NEXT_SYNC_LIMIT = 20;

    private final EmailAccountRepository emailAccountRepository;
    private final UserRepository userRepository;
    private final EmailRepository emailRepository;
    private final CryptoUtil cryptoUtil;
    private final EmailSaveService emailSaveService;

    // 이메일 계정 등록
    @Transactional
    public EmailAccountResponseDto createEmailAccount(Long userId, EmailAccountRequestDto request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        String email = request.getEmail().trim().toLowerCase();

        if (emailAccountRepository.existsByUserAndEmail(user, email)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 등록된 이메일 계정입니다.");
        }

        EmailProvider provider = parseProvider(request.getProvider());

        ImapConfig imapConfig = resolveImapConfig(provider, request);

        byte[] secretEnc;
        try {
            secretEnc = cryptoUtil.encrypt(request.getSecret());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "이메일 비밀번호 암호화 중 오류가 발생했습니다.");
        }

        EmailAccount emailAccount = EmailAccount.builder()
                .user(user)
                .email(email)
                .provider(provider.name())
                .imapHost(imapConfig.host())
                .imapPort(imapConfig.port())
                .loginId(request.getLoginId().trim())
                .secretEnc(secretEnc)
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
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        return emailAccountRepository.findAllByUser(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 이메일 계정 삭제
    @Transactional
    public void deleteEmailAccount(Long userId, Long accountId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "삭제할 이메일 계정을 찾을 수 없습니다."));

        EmailAccount emailAccount = emailAccountRepository.findByAccountIdAndUser(accountId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "삭제할 이메일 계정을 찾을 수 없습니다."));

        emailAccountRepository.delete(emailAccount);
    }

    // 이메일 동기화
    @Transactional
    public Map<String, Object> syncEmails(Long userId, Long accountId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        EmailAccount account = emailAccountRepository.findByAccountIdAndUser(accountId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "이메일 계정을 찾을 수 없습니다."));

        if (Boolean.FALSE.equals(account.getActive())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "비활성화된 이메일 계정입니다.");
        }

        Store store = null;
        Folder inbox = null;

        int collectedEmailCount = 0;
        int savedEmailCount = 0;
        int skippedEmailCount = 0;
        int extractedUrlCount = 0;

        try {
            // System.out.println("========== SYNC START ==========");
            // System.out.println("accountId = " + account.getAccountId());
            // System.out.println("provider = " + account.getProvider());
            // System.out.println("email = " + account.getEmail());
            // System.out.println("loginId = " + account.getLoginId());
            // System.out.println("imapHost = " + account.getImapHost());
            // System.out.println("imapPort = " + account.getImapPort());

            // 마지막 동기화 시각 조회
            LocalDateTime lastSyncedAt = account.getLastSyncedAt();

            // IMAP 세션 설정
            Properties props = new Properties();
            props.put("mail.store.protocol", "imap");
            props.put("mail.imap.host", account.getImapHost());
            props.put("mail.imap.port", String.valueOf(account.getImapPort()));
            props.put("mail.imap.ssl.enable", "true");
            props.put("mail.imap.ssl.trust", "*");

            Session session = Session.getInstance(props);
            store = session.getStore("imap");

            // 비밀번호 복호화 후 접속
            String secret = cryptoUtil.decrypt(account.getSecretEnc());

            System.out.println("[DEBUG] host=" + account.getImapHost()
                    + ", port=" + account.getImapPort()
                    + ", loginId=" + account.getLoginId()
                    + ", secretLen=" + (secret == null ? "null" : secret.length()));

            store.connect(account.getImapHost(), account.getLoginId(), secret);

            // IMAP에서 접근 가능한 폴더 목록 확인
            Folder defaultFolder = store.getDefaultFolder();
            Folder[] folders = defaultFolder.list("*");

            for (Folder folder : folders) {
                try {
                    folder.open(Folder.READ_ONLY);

                    System.out.println(
                            "[IMAP FOLDER] "
                                    + folder.getFullName()
                                    + " / messageCount = "
                                    + folder.getMessageCount());

                    folder.close(false);

                } catch (Exception ignored) {
                }
            }

            // store.connect(
            // account.getImapHost(),
            // account.getLoginId(),
            // secret
            // );

            // INBOX 열기
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();

            System.out.println("[SYNC] totalMessages = " + messages.length);
            System.out.println("[SYNC] lastSyncedAt = " + lastSyncedAt);

            // 마지막 동기화 시간이 없으면 최초 동기화로 판단
            boolean isFirstSync = (lastSyncedAt == null);

            // 최초 동기화는 최대 100개, 이후 동기화는 전체 메일 확인
            int syncLimit = isFirstSync ? FIRST_SYNC_LIMIT : messages.length;

            // IMAP 메시지는 보통 오래된 메일 -> 최신 메일 순서로 들어오기 때문에 뒤에서부터 최신 메일을 확인
            int startIndex = messages.length - 1;

            // 가져올 범위의 마지막 index 계산
            // 예: 전체 200개, limit 100이면 index 199부터 100까지 총 100개 처리
            int endIndex = Math.max(0, messages.length - syncLimit);

            // System.out.println("[SYNC] firstSync = " + isFirstSync);
            System.out.println("[SYNC] syncLimit = " + syncLimit);
            System.out.println("[SYNC] totalMessages = " + messages.length);

            // sync 시작 - 100개 / 이후 10개
            for (int i = startIndex; i >= endIndex; i--) {

                Message msg = messages[i];

                System.out.println("[SYNC] processing subject = " + msg.getSubject());

                collectedEmailCount++;

                LocalDateTime receivedAt = msg.getReceivedDate() != null
                        ? LocalDateTime.ofInstant(
                                msg.getReceivedDate().toInstant(),
                                ZoneId.systemDefault())
                        : LocalDateTime.now();

                /*
                 * 최초 동기화가 아닌 경우, 마지막 동기화 시각 이전 메일은 저장하지 않음
                 * if (lastSyncedAt != null && !receivedAt.isAfter(lastSyncedAt)) {
                 * skippedEmailCount++;
                 * continue;
                 * }
                 */

                String messageUid = buildMessageUid(msg);

                System.out.println("[SYNC] messageUid = " + messageUid);

                boolean exists = emailRepository.existsByMessageUid(messageUid);
                System.out.println("[SYNC] exists = " + exists);

                // 이미 저장된 메일이면 중복 저장하지 않고 skip
                if (exists) {
                    skippedEmailCount++;
                    continue;
                }

                String subject = msg.getSubject();

                // HTML 본문과 텍스트 본문을 따로 추출
                EmailBody emailBody = getEmailBody(msg);

                String bodyText = emailBody.bodyText();
                String bodyHtml = emailBody.bodyHtml();

                // URL은 텍스트 본문 + HTML 본문 둘 다에서 추출
                List<String> extractedUrls = extractUrls(
                        (bodyText != null ? bodyText : "") + " " + (bodyHtml != null ? bodyHtml : ""));

                // 메일 1건 처리 실패가 sync 전체를 죽이지 않게 try-catch로 감쌈
                try {
                    int savedUrlCount = emailSaveService.saveEmailAndUrls(
                            userId,
                            account,
                            messageUid,
                            extractSenderName(msg),
                            extractSenderEmail(msg),
                            account.getEmail(),
                            subject,
                            bodyText,
                            bodyHtml,
                            receivedAt,
                            extractedUrls);

                    System.out.println("[SYNC] email saved, savedUrlCount = " + savedUrlCount);

                    savedEmailCount++;
                    extractedUrlCount += savedUrlCount;
                } catch (Exception perMailEx) {
                    skippedEmailCount++;
                    // 로깅 권장 (log.warn 또는 디버깅 중에는 printStackTrace)
                    perMailEx.printStackTrace();
                }

                System.out.println("[SYNC] processing = " + msg.getSubject());

            }

            // 성공 처리
            account.updateSyncSuccess();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("accountId", account.getAccountId());
            result.put("collectedEmailCount", collectedEmailCount);
            result.put("savedEmailCount", savedEmailCount);
            result.put("skippedEmailCount", skippedEmailCount);
            result.put("extractedUrlCount", extractedUrlCount);
            result.put("lastSyncedAt", account.getLastSyncedAt());

            // System.out.println("========== SYNC SUCCESS ==========");

            return result;

        } catch (Exception e) {
            account.updateSyncFailed();

            // System.out.println("========== SYNC ERROR ==========");
            e.printStackTrace();
            // System.out.println("========== SYNC ERROR END ==========");

            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "이메일 동기화 중 오류가 발생했습니다.");

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
            return EmailProvider.valueOf(provider.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 provider입니다.");
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
                    throw new BusinessException(ErrorCode.INVALID_INPUT, "CUSTOM provider는 imapHost가 필요합니다.");
                }

                if (request.getImapPort() == null) {
                    throw new BusinessException(ErrorCode.INVALID_INPUT, "CUSTOM provider는 imapPort가 필요합니다.");
                }

                return new ImapConfig(request.getImapHost().trim(), request.getImapPort());

            default:
                throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 provider입니다.");
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

    // 이메일 본문 추출 결과를 담는 record
    private record EmailBody(String bodyText, String bodyHtml) {
    }

    // 이메일 본문 추출
    private EmailBody getEmailBody(Part part) throws Exception {

        // 일반 텍스트 메일
        if (part.isMimeType("text/plain")) {
            Object content = part.getContent();
            String text = content != null ? content.toString() : "";
            return new EmailBody(text, null);
        }

        // HTML 메일
        if (part.isMimeType("text/html")) {
            Object content = part.getContent();
            String html = content != null ? content.toString() : "";
            String text = htmlToText(html);

            // HTML 원본은 bodyHtml에 저장
            return new EmailBody(text, html);
        }

        // multipart 메일
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();

            String bodyText = "";
            String bodyHtml = "";

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                EmailBody childBody = getEmailBody(bodyPart);

                // text/plain 본문 저장
                if (bodyText.isBlank()
                        && childBody.bodyText() != null
                        && !childBody.bodyText().isBlank()) {
                    bodyText = childBody.bodyText();
                }

                // text/html 본문 저장
                if (bodyHtml.isBlank()
                        && childBody.bodyHtml() != null
                        && !childBody.bodyHtml().isBlank()) {
                    bodyHtml = childBody.bodyHtml();
                }
            }

            // text/plain이 없고 HTML만 있으면, HTML을 텍스트로 바꿔서 bodyText에도 저장
            if (bodyText.isBlank() && !bodyHtml.isBlank()) {
                bodyText = htmlToText(bodyHtml);
            }

            return new EmailBody(bodyText, bodyHtml);
        }

        return new EmailBody("", null);
    }

    // HTML 태그 제거 후 텍스트 변환
    private String htmlToText(String html) {

        if (html == null) {
            return "";
        }

        return html
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("<[^>]*>", "")
                .trim();
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

            if (froms[0] instanceof InternetAddress internetAddress) {
                return internetAddress.getAddress();
            }

            return froms[0].toString();

        } catch (Exception e) {
            return null;
        }
    }

    // 발신자 이름 추출
    private String extractSenderName(Message msg) {
        try {
            Address[] froms = msg.getFrom();

            if (froms == null || froms.length == 0) {
                return null;
            }

            if (froms[0] instanceof InternetAddress internetAddress) {
                return internetAddress.getPersonal();
            }

            return null;

        } catch (Exception e) {
            return null;
        }
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