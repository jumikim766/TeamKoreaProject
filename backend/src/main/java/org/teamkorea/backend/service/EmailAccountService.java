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

import jakarta.mail.internet.MimeUtility;
import jakarta.mail.internet.InternetAddress;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAccountService {

    private final EmailAccountRepository emailAccountRepository;
    private final UserRepository userRepository;
    private final EmailRepository emailRepository;
    private final CryptoUtil cryptoUtil;
    private final EmailSaveService emailSaveService;
    private final EmailSyncStatusService emailSyncStatusService;

    // 최초 동기화 시 100개씩 끊어서 처리
    private static final int FIRST_SYNC_BATCH_SIZE = 100;

    // 일반 동기화 시 최신 30개만 확인
    private static final int NORMAL_SYNC_LIMIT = 30;

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

        // 이메일 연동 전에 실제 IMAP 로그인 가능한지 검증
        // 잘못된 email/loginId/secret이면 DB 저장하지 않음
        validateImapConnection(
                imapConfig.host(),
                imapConfig.port(),
                request.getLoginId().trim(),
                request.getSecret());

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

    // 수동 sync 요청 전 계정 존재/권한/활성 상태만 빠르게 검증
    @Transactional(readOnly = true)
    public void validateSyncRequest(Long userId, Long accountId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        EmailAccount account = emailAccountRepository.findByAccountIdAndUser(accountId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "이메일 계정을 찾을 수 없습니다."));

        if (Boolean.FALSE.equals(account.getActive())) {
            throw new BusinessException(
                    ErrorCode.EMAIL_ACCOUNT_INACTIVE,
                    "비활성화된 이메일 계정입니다. 재연동이 필요합니다.");
        }
    }

    // 이메일 동기화
    // syncEmails 전체를 하나의 긴 트랜잭션으로 묶지 않음
    // 메일 1건 저장은 EmailSaveService에서 처리
    public Map<String, Object> syncEmails(Long userId, Long accountId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        EmailAccount account = emailAccountRepository.findByAccountIdAndUser(accountId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "이메일 계정을 찾을 수 없습니다."));

        if (Boolean.FALSE.equals(account.getActive())) {
            throw new BusinessException(ErrorCode.EMAIL_ACCOUNT_INACTIVE, "비활성화된 이메일 계정입니다.");
        }

        Store store = null;
        Folder inbox = null;

        int collectedEmailCount = 0;
        int savedEmailCount = 0;
        int skippedEmailCount = 0;
        int extractedUrlCount = 0;

        try {
            // 마지막 동기화 시각 조회
            LocalDateTime lastSyncedAt = account.getLastSyncedAt();

            // IMAP 세션 설정
            Properties props = new Properties();
            props.put("mail.store.protocol", "imap");
            props.put("mail.imap.host", account.getImapHost());
            props.put("mail.imap.port", String.valueOf(account.getImapPort()));
            props.put("mail.imap.ssl.enable", "true");
            props.put("mail.imap.ssl.trust", "*");
            props.put("mail.imap.connectiontimeout", "5000");
            props.put("mail.imap.timeout", "5000");
            props.put("mail.imap.writetimeout", "5000");

            Session session = Session.getInstance(props);
            store = session.getStore("imap");

            // 비밀번호 복호화 후 접속
            String secret = cryptoUtil.decrypt(account.getSecretEnc());

            store.connect(account.getImapHost(), account.getLoginId(), secret);

            // // IMAP에서 접근 가능한 폴더 목록 확인
            // Folder defaultFolder = store.getDefaultFolder();
            // Folder[] folders = defaultFolder.list("*");

            // for (Folder folder : folders) {
            // try {
            // folder.open(Folder.READ_ONLY);

            // folder.close(false);

            // } catch (Exception ignored) {
            // }
            // }

            // store.connect(
            // account.getImapHost(),
            // account.getLoginId(),
            // secret
            // );

            // INBOX 열기
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            int totalMessageCount = inbox.getMessageCount();

            // 마지막 동기화 시간이 없으면 최초 동기화로 판단
            boolean isFirstSync = (lastSyncedAt == null);

            // 최초 sync는 전체 메일을 100개씩 끊어서 처리
            // 이후 sync는 최신 30개만 확인
            int startMessageNumber = totalMessageCount;
            int endMessageNumber = isFirstSync
                    ? 1
                    : Math.max(totalMessageCount - NORMAL_SYNC_LIMIT + 1, 1);

            mailLoop: for (int batchEnd = startMessageNumber; batchEnd >= endMessageNumber; batchEnd -= FIRST_SYNC_BATCH_SIZE) {

                int batchStart = Math.max(batchEnd - FIRST_SYNC_BATCH_SIZE + 1, endMessageNumber);

                // JavaMail message number는 1부터 시작
                Message[] batchMessages = inbox.getMessages(batchStart, batchEnd);

                // 최신 메일부터 처리하기 위해 역순
                for (int j = batchMessages.length - 1; j >= 0; j--) {

                    Message msg = batchMessages[j];

                    collectedEmailCount++;

                    LocalDateTime receivedAt = msg.getReceivedDate() != null
                            ? LocalDateTime.ofInstant(
                                    msg.getReceivedDate().toInstant(),
                                    ZoneId.systemDefault())
                            : LocalDateTime.now();

                    // 최초 sync가 아닌 경우 lastSyncedAt 이후 메일만 확인
                    // 최신 메일부터 확인 중이므로, 이전 메일을 만나면 전체 batch 반복 종료
                    if (!isFirstSync && lastSyncedAt != null && !receivedAt.isAfter(lastSyncedAt)) {
                        break mailLoop;
                    }

                    String messageUid = buildMessageUid(msg);

                    boolean exists = emailRepository.existsByMessageUid(messageUid);

                    // 이미 저장된 메일이면 중복 저장하지 않고 skip
                    if (exists) {
                        skippedEmailCount++;
                        continue;
                    }

                    String subject = msg.getSubject();

                    EmailBody emailBody = getEmailBody(msg);

                    String bodyText = emailBody.bodyText();
                    String bodyHtml = emailBody.bodyHtml();

                    List<String> extractedUrls = extractUrls(
                            (bodyText != null ? bodyText : "") + " " + (bodyHtml != null ? bodyHtml : ""));

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

                        savedEmailCount++;
                        extractedUrlCount += savedUrlCount;

                    } catch (Exception perMailEx) {
                        skippedEmailCount++;

                        log.warn("[EMAIL SYNC] 메일 1건 저장 실패 - accountId={}, messageUid={}, reason={}",
                                account.getAccountId(), messageUid, perMailEx.getMessage());

                        continue;
                    }
                }
            }

            // 성공 처리
            emailSyncStatusService.markSuccess(account.getAccountId());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("accountId", account.getAccountId());
            result.put("collectedEmailCount", collectedEmailCount);
            result.put("savedEmailCount", savedEmailCount);
            result.put("skippedEmailCount", skippedEmailCount);
            result.put("extractedUrlCount", extractedUrlCount);
            result.put("lastSyncedAt", LocalDateTime.now());

            return result;

        } catch (BusinessException e) {
            emailSyncStatusService.markFailed(account.getAccountId());

            throw e;

        } catch (AuthenticationFailedException e) {
            emailSyncStatusService.markAuthFailedAndDeactivate(account.getAccountId());

            throw new BusinessException(
                    ErrorCode.EMAIL_AUTH_FAILED,
                    "이메일 로그인 정보가 올바르지 않습니다. 이메일 주소, 로그인 ID, 앱 비밀번호를 확인해주세요.");

        } catch (MessagingException e) {
            emailSyncStatusService.markFailed(account.getAccountId());

            throw new BusinessException(
                    ErrorCode.EMAIL_CONNECT_FAILED,
                    "IMAP 서버 연결 또는 메일함 접근에 실패했습니다. IMAP 설정을 확인해주세요.");

        } catch (Exception e) {
            emailSyncStatusService.markFailed(account.getAccountId());

            throw new BusinessException(
                    ErrorCode.EMAIL_SYNC_FAILED,
                    "이메일 동기화 중 오류가 발생했습니다.");
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

        // script/style 제거 후 HTML 태그 제거
        return html
                .replaceAll("(?is)<script.*?>.*?</script>", "")
                .replaceAll("(?is)<style.*?>.*?</style>", "")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("<[^>]*>", "")
                .trim();
    }

    // 본문에서 URL 추출
    private List<String> extractUrls(String text) {

        Pattern pattern = Pattern.compile("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+");
        Matcher matcher = pattern.matcher(text == null ? "" : text);

        // 같은 메일 안에서 중복 URL 제거
        return matcher.results()
                .map(match -> match.group())
                .distinct()
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
                // MIME 인코딩된 발신자 이름 디코딩
                return internetAddress.getPersonal() != null
                        ? MimeUtility.decodeText(internetAddress.getPersonal())
                        : null;
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
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "메일 고유값 생성 중 오류가 발생했습니다.");
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

    // 이메일 연동 등록 전 실제 IMAP 로그인 검증
    private void validateImapConnection(
            String imapHost,
            int imapPort,
            String loginId,
            String secret) {

        Store testStore = null;

        try {

            Properties props = new Properties();

            props.put("mail.store.protocol", "imap");
            props.put("mail.imap.host", imapHost);
            props.put("mail.imap.port", String.valueOf(imapPort));
            props.put("mail.imap.ssl.enable", "true");
            props.put("mail.imap.ssl.trust", "*");

            // 연결 timeout
            props.put("mail.imap.connectiontimeout", "5000");
            props.put("mail.imap.timeout", "5000");
            props.put("mail.imap.writetimeout", "5000");

            Session session = Session.getInstance(props);

            testStore = session.getStore("imap");

            // 실제 로그인 시도
            testStore.connect(imapHost, loginId, secret);

        } catch (AuthenticationFailedException e) {

            // 로그인 실패
            throw new BusinessException(
                    ErrorCode.EMAIL_AUTH_FAILED,
                    "이메일 로그인 정보가 올바르지 않습니다.");
        } catch (MessagingException e) {

            // 서버 연결 실패
            throw new BusinessException(
                    ErrorCode.EMAIL_CONNECT_FAILED,
                    "IMAP 서버 연결에 실패했습니다. 이메일 또는 IMAP 설정을 확인해주세요.");

        } catch (Exception e) {

            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "이메일 연동 검증 중 오류가 발생했습니다.");

        } finally {

            try {
                if (testStore != null && testStore.isConnected()) {
                    testStore.close();
                }
            } catch (Exception ignored) {
            }
        }
    }
}