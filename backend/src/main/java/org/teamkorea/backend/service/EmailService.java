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
import org.teamkorea.backend.repository.UrlAnalysisRepository;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailService {

    private final EmailRepository emailRepository;
    private final EmailUrlRepository emailUrlRepository;
    private final UrlAnalysisRepository urlAnalysisRepository;

    // 이메일 목록 조회
    public Page<EmailListResponseDto> getEmails(
            Long userId, Long accountId, String keyword, LocalDateTime receivedAtFrom, LocalDateTime receivedAtTo, int page, int size) {

        // 페이지네이션 설정
        Pageable pageable = PageRequest.of(page, size);

        // 빈 문자열 keyword는 검색 조건에서 제외
        String searchKeyword = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchKeyword = keyword.trim();
        }
        
        // 현재 로그인한 사용자 기준으로만 이메일 조회
        Page<Email> emails = emailRepository.searchEmailByUser(
            userId, accountId, searchKeyword, receivedAtFrom, receivedAtTo, pageable);
        

        return emails.map(this::toEmailListResponse);
    }

    // 이메일 상세 조회
    public EmailDetailResponseDto getEmailDetail(Long userId, Long emailId) {

        Email email = emailRepository.findByIdWithAccount(emailId)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일을 찾을 수 없습니다."));

        // 현재 로그인한 사용자의 이메일인지 검증
        validateEmailOwner(email, userId);

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

    // 특정 이메일 URL 목록 조회
    public List<EmailUrlResponseDto> getEmailUrls(Long userId, Long emailId) {

        Email email = emailRepository.findByIdWithAccount(emailId)
            .orElseThrow(() -> new IllegalArgumentException("해당 이메일을 찾을 수 없습니다."));
        
        validateEmailOwner(email, userId);

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

    // 이메일 URL DTO 변환
    private EmailUrlResponseDto toEmailUrlResponse(EmailUrl emailUrl) {

        Url url = emailUrl.getUrl();

        String riskLevel = urlAnalysisRepository
                .findTopByUrl_UrlIdOrderByAnalyzedAtDesc(url.getUrlId())
                .map(analysis -> analysis.getRiskLevel().name())
                .orElse("UNKNOWN");

        return EmailUrlResponseDto.builder()
                .urlId(url.getUrlId())
                .originalUrl(emailUrl.getRawUrl())
                .normalizedUrl(url.getNormalizedUrl())
                .domain(url.getDomain())
                .riskLevel(riskLevel)
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

    // 이메일이 현재 로그인한 사용자의 것인지 검증
    private void validateEmailOwner(Email email, Long userId) {

        if(email.getAccount() == null ||
                email.getAccount().getUser() == null ||
                !email.getAccount().getUser().getUserId().equals(userId)) {

            throw new AccessDeniedException("해당 이메일에 접근할 권한이 없습니다.");
                }
    }
}