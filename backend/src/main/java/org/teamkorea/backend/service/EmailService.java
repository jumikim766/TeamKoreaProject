package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.Email;
import org.teamkorea.backend.domain.EmailUrl;
import org.teamkorea.backend.domain.Url;
import org.teamkorea.backend.dto.EmailDetailResponseDto;
import org.teamkorea.backend.dto.EmailListResponseDto;
import org.teamkorea.backend.dto.EmailUrlResponseDto;
import org.teamkorea.backend.repository.EmailRepository;
import org.teamkorea.backend.repository.EmailUrlRepository;
import org.teamkorea.backend.domain.UrlAnalysis;
import org.teamkorea.backend.repository.UrlAnalysisRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailService {

    private final EmailRepository emailRepository;
    private final EmailUrlRepository emailUrlRepository;
    private final UrlAnalysisRepository urlAnalysisRepository;

    // 이메일 목록 조회
    public Page<EmailListResponseDto> getEmails(Long accountId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Email> emails;

        // accountId가 있으면 특정 이메일 계정 기준 조회
        if (accountId != null) {
            emails = emailRepository.findByAccount_AccountId(accountId, pageable);
        } else {
            emails = emailRepository.findAll(pageable);
        }

        // Email 엔티티를 목록 DTO로 변환
        return emails.map(this::toEmailListResponse);
    }

    // 이메일 상세 조회
    public EmailDetailResponseDto getEmailDetail(Long emailId) {

        Email email = emailRepository.findByIdWithAccount(emailId)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일을 찾을 수 없습니다."));

        int urlCount = emailUrlRepository.countByEmail_EmailId(emailId);

        return EmailDetailResponseDto.builder()
                .emailId(email.getEmailId())
                .accountId(email.getAccount() != null ? email.getAccount().getAccountId() : null)
                .senderEmail(email.getSenderEmail())
                .senderName(email.getSenderName() != null ? email.getSenderName() : "")
                .receiverEmail(email.getReceiverEmail())
                .subject(email.getSubject() != null ? email.getSubject() : "")
                .bodyText(email.getBodyText() != null ? email.getBodyText() : "")
                .receivedAt(email.getReceivedAt())
                .createdAt(email.getCreatedAt())
                .urlCount(urlCount)
                .build();
    }

    // 특정 이메일의 URL 목록 조회
    public List<EmailUrlResponseDto> getEmailUrls(Long emailId) {

        // 이메일 존재 여부 확인
        emailRepository.findById(emailId)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일을 찾을 수 없습니다."));

        List<EmailUrl> emailUrls = emailUrlRepository.findByEmailIdWithUrl(emailId);

        return emailUrls.stream()
                .filter(emailUrl -> emailUrl != null && emailUrl.getUrl() != null)
                .map(this::toEmailUrlResponse)
                .toList();
    }

    // 이메일 목록 DTO 변환
    private EmailListResponseDto toEmailListResponse(Email email) {

        return EmailListResponseDto.builder()
                .emailId(email.getEmailId())
                .senderName(email.getSenderName() != null ? email.getSenderName() : "")
                .subject(email.getSubject() != null ? email.getSubject() : "")
                .previewText(makePreviewText(email.getBodyText()))
                .receivedAt(email.getReceivedAt())
                .build();
    }

    private EmailUrlResponseDto toEmailUrlResponse(EmailUrl emailUrl) {

    Url url = emailUrl.getUrl();

    UrlAnalysis latestAnalysis = urlAnalysisRepository
            .findTopByUrl_UrlIdOrderByAnalyzedAtDesc(url.getUrlId())
            .orElse(null);

    return EmailUrlResponseDto.builder()
            .urlId(url.getUrlId())
            .originalUrl(emailUrl.getRawUrl())
            .normalizedUrl(url.getNormalizedUrl())
            .domain(url.getDomain())
            .riskLevel(latestAnalysis != null ? latestAnalysis.getRiskLevel().name() : null)
            .reasonSummary(latestAnalysis != null ? latestAnalysis.getReasonSummary() : null)
            .score(latestAnalysis != null ? latestAnalysis.getScore() : null)
            .build();
}

    // 메일 목록 미리보기 생성
    private String makePreviewText(String bodyText) {

        if (bodyText == null || bodyText.isBlank()) {
            return "";
        }

        String preview = bodyText.replaceAll("\\s+", " ").trim();

        if (preview.length() > 100) {
            return preview.substring(0, 100);
        }

        return preview;
    }
}