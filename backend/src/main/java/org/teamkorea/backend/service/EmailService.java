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
import org.teamkorea.backend.domain.RiskLevel;
import org.teamkorea.backend.domain.UrlAnalysis;
import org.teamkorea.backend.exception.BusinessException;
import org.teamkorea.backend.exception.ErrorCode;

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
            Long userId, Long accountId, String keyword, LocalDateTime receivedAtFrom, LocalDateTime receivedAtTo,
            int page, int size) {

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
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 이메일을 찾을 수 없습니다."));

        // 현재 로그인한 사용자의 이메일인지 검증
        validateEmailOwner(email, userId);

        int urlCount = emailUrlRepository.countByEmail_EmailId(emailId);

        // 이메일에 포함된 URL 중 가장 높은 위험도 계산
        String riskLevel = calculateEmailRiskLevel(emailId);

        return EmailDetailResponseDto.builder()
                .emailId(email.getEmailId())
                .accountId(email.getAccount() != null ? email.getAccount().getAccountId() : null)
                .senderName(getDisplaySender(email))
                .senderEmail(email.getSenderEmail())
                .receiverEmail(email.getReceiverEmail())
                .subject(email.getSubject() != null ? email.getSubject() : "")
                .bodyText(email.getBodyText() != null ? email.getBodyText() : "")
                .bodyHtml(email.getBodyHtml())
                .receivedAt(email.getReceivedAt())
                .createdAt(email.getCreatedAt())
                .urlCount(urlCount)
                .riskLevel(riskLevel)
                .build();
    }

    // 특정 이메일 URL 목록 조회
    public List<EmailUrlResponseDto> getEmailUrls(Long userId, Long emailId) {

        Email email = emailRepository.findByIdWithAccount(emailId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 이메일을 찾을 수 없습니다."));

        validateEmailOwner(email, userId);

        List<EmailUrl> emailUrls = emailUrlRepository.findByEmailIdWithUrl(emailId);

        return emailUrls.stream()
                .filter(emailUrl -> emailUrl != null && emailUrl.getUrl() != null)
                .map(this::toEmailUrlResponse)
                .toList();
    }

    // 이메일 목록 DTO 변환
    private EmailListResponseDto toEmailListResponse(Email email) {
        String displaySender = getDisplaySender(email);

        return EmailListResponseDto.builder()
                .emailId(email.getEmailId())
                .senderName(displaySender)
                .senderEmail(email.getSenderEmail())
                .subject(email.getSubject() != null ? email.getSubject() : "")
                .previewText(makePreviewText(email.getBodyText()))
                .receivedAt(email.getReceivedAt())
                .build();
    }

    // 발신자 표시명 생성
    private String getDisplaySender(Email email) {
        if (email.getSenderName() != null && !email.getSenderName().isBlank()) {
            return email.getSenderName();
        }

        if (email.getSenderEmail() != null && !email.getSenderEmail().isBlank()) {
            return email.getSenderEmail();
        }

        return "알 수 없음";
    }

    // 이메일 URL DTO 변환
    private EmailUrlResponseDto toEmailUrlResponse(EmailUrl emailUrl) {

        Url url = emailUrl.getUrl();

        // 최신 URL 분석 결과 조회
        // 명세서의 riskLevel, reasonSummary, score를 내려주기 위해 UrlAnalysis를 한 번만 조회
        UrlAnalysis latestAnalysis = urlAnalysisRepository
                .findTopByUrl_UrlIdOrderByAnalyzedAtDesc(url.getUrlId())
                .orElse(null);

        return EmailUrlResponseDto.builder()
                .urlId(url.getUrlId())
                .originalUrl(emailUrl.getRawUrl())
                .normalizedUrl(url.getNormalizedUrl())
                .domain(url.getDomain())
                .riskLevel(latestAnalysis != null ? latestAnalysis.getRiskLevel().name() : "SAFE")
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

        // 프론트 메일 리스트 UI에서 너무 긴 미리보기 방지
        if (preview.length() > 100) {
            return preview.substring(0, 100) + "...";
        }

        return preview;
    }

    // 이메일에 포함된 URL 중 가장 높은 위험도 계산
    private String calculateEmailRiskLevel(Long emailId) {

        List<EmailUrl> emailUrls = emailUrlRepository.findByEmailIdWithUrl(emailId);

        RiskLevel highestRiskLevel = RiskLevel.SAFE;

        for (EmailUrl emailUrl : emailUrls) {
            if (emailUrl == null || emailUrl.getUrl() == null) {
                continue;
            }

            RiskLevel currentRiskLevel = urlAnalysisRepository
                    .findTopByUrl_UrlIdOrderByAnalyzedAtDesc(emailUrl.getUrl().getUrlId())
                    .map(analysis -> analysis.getRiskLevel())
                    .orElse(RiskLevel.SAFE);

            if (getRiskPriority(currentRiskLevel) > getRiskPriority(highestRiskLevel)) {
                highestRiskLevel = currentRiskLevel;
            }
        }

        return highestRiskLevel.name();
    }

    // 위험도 우선순위
    private int getRiskPriority(RiskLevel riskLevel) {
        if (riskLevel == null) {
            return 0;
        }

        return switch (riskLevel) {
            case SAFE -> 1;
            case WARNING -> 2;
            case DANGER -> 3;
            case CRITICAL -> 4;
        };
    }

    // 이메일이 현재 로그인한 사용자의 것인지 검증
    private void validateEmailOwner(Email email, Long userId) {

        if (email.getAccount() == null ||
                email.getAccount().getUser() == null ||
                !email.getAccount().getUser().getUserId().equals(userId)) {

            // 공통 예외 응답 형식으로 통일
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 이메일에 접근할 권한이 없습니다.");
        }
    }
}