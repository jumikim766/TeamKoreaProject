package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.Url;
import org.teamkorea.backend.domain.UrlAnalysis;
import org.teamkorea.backend.dto.UrlDetailResponseDto;
import org.teamkorea.backend.dto.UrlListItemResponseDto;
import org.teamkorea.backend.dto.UrlListResponseDto;
import org.teamkorea.backend.repository.UrlAnalysisRepository;
import org.teamkorea.backend.repository.UrlRepository;
import org.teamkorea.backend.domain.RiskLevel;
import org.teamkorea.backend.domain.EmailUrl;
import org.teamkorea.backend.dto.MyUrlItemResponseDto;
import org.teamkorea.backend.dto.MyUrlListResponseDto;
import org.teamkorea.backend.dto.UrlStatisticsResponseDto;
import org.teamkorea.backend.exception.BusinessException;
import org.teamkorea.backend.exception.ErrorCode;
import org.teamkorea.backend.repository.EmailUrlRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UrlService {

        private final UrlRepository urlRepository;
        private final UrlAnalysisRepository urlAnalysisRepository;
        private final EmailUrlRepository emailUrlRepository;

        // URL 목록 조회
        public UrlListResponseDto getUrls(
                        String domain,
                        String riskLevel,
                        Boolean isAnalyzed,
                        int page,
                        int size) {
                Pageable pageable = PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.DESC, "createdAt"));

                String searchDomain = null;
                if (domain != null && !domain.trim().isEmpty()) {
                        searchDomain = domain.trim();
                }

                RiskLevel searchRiskLevel = parseRiskLevel(riskLevel);

                Page<Url> urlPage = urlRepository.searchUrls(searchDomain, searchRiskLevel, isAnalyzed, pageable);

                List<UrlListItemResponseDto> urls = urlPage.getContent().stream()
                                .map(this::toListItem)
                                .toList();

                return new UrlListResponseDto(
                                urls,
                                urlPage.getNumber(),
                                urlPage.getSize(),
                                urlPage.getTotalElements(),
                                urlPage.getTotalPages());
        }

        // URL 상세 조회 (에러 해결: 빌더 패턴 적용 완료)
        public UrlDetailResponseDto getUrlDetail(Long urlId) {
                Url url = urlRepository.findById(urlId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.URL_NOT_FOUND,
                                                "해당 URL 정보를 찾을 수 없습니다."));

                Optional<UrlAnalysis> latestAnalysis = urlAnalysisRepository
                                .findTopByUrl_UrlIdOrderByAnalyzedAtDesc(urlId);

                String riskLevel = latestAnalysis
                                .map(analysis -> analysis.getRiskLevel().name())
                                .orElse("SAFE");

                String reasonSummary = latestAnalysis
                                .map(UrlAnalysis::getReasonSummary)
                                .orElse(null);

                List<EmailUrl> emailUrls = emailUrlRepository.findByUrlIdWithEmail(urlId);

                String senderName = null;
                String senderEmail = null;
                String originalUrl = url.getNormalizedUrl();

                if (!emailUrls.isEmpty()) {
                        EmailUrl emailUrl = emailUrls.get(0);
                        senderName = emailUrl.getEmail().getSenderName();
                        senderEmail = emailUrl.getEmail().getSenderEmail();
                        originalUrl = emailUrl.getRawUrl();
                }

                return UrlDetailResponseDto.builder()
                        .urlId(url.getUrlId())
                        .senderName(senderName)
                        .senderEmail(senderEmail)
                        .originalUrl(originalUrl)
                        .normalizedUrl(url.getNormalizedUrl())
                        .domain(url.getDomain())
                        .riskLevel(riskLevel)
                        .reasonSummary(reasonSummary)
                        .createdAt(url.getCreatedAt())
                        .updatedAt(url.getLastSeenAt()) 
                        .build();
        }

        // 나의 URL 목록 조회
        public MyUrlListResponseDto getMyUrls(
                        Long userId,
                        Long accountId,
                        String domain,
                        String riskLevel,
                        Boolean isAnalyzed,
                        int page,
                        int size) {
                Pageable pageable = PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.DESC, "createdAt"));

                String searchDomain = normalizeSearchText(domain);
                RiskLevel searchRiskLevel = parseRiskLevel(riskLevel);

                Page<EmailUrl> emailUrlPage = emailUrlRepository.searchMyUrls(
                                userId,
                                accountId,
                                searchDomain,
                                searchRiskLevel,
                                isAnalyzed,
                                pageable);

                List<MyUrlItemResponseDto> urls = emailUrlPage.getContent().stream()
                                .map(this::toMyUrlItem)
                                .toList();

                return new MyUrlListResponseDto(
                                urls,
                                emailUrlPage.getNumber(),
                                emailUrlPage.getSize(),
                                emailUrlPage.getTotalElements(),
                                emailUrlPage.getTotalPages());
        }

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        public UrlStatisticsResponseDto getUrlStatistics(
                        Long userId,
                        String scope,
                        Long accountId,
                        String domain,
                        Boolean isAnalyzed,
                        String period) {
                String searchDomain = normalizeSearchText(domain);

                List<Url> urls;

                String normalizedPeriod = normalizePeriod(period);
                LocalDateTime startDateTime = getStatisticsStartDateTime(normalizedPeriod);

                if ("MY".equalsIgnoreCase(scope)) {
                        urls = emailUrlRepository.findMyUrlsForStatistics(
                                        userId,
                                        accountId,
                                        searchDomain,
                                        isAnalyzed)
                                        .stream()
                                        .map(EmailUrl::getUrl)
                                        .toList();
                } else {
                        urls = urlRepository.findUrlsForStatistics(searchDomain, isAnalyzed);
                }

                if (startDateTime != null) {
                        urls = urls.stream()

                                .filter(url -> url.getCreatedAt() != null)
                                .filter(url -> !url.getCreatedAt().isBefore(todayStart))
                                .toList();

                }

                long totalCount = urls.size();
                long criticalCount = 0;
                long dangerCount = 0;
                long warningCount = 0;
                long safeCount = 0;
                long unanalyzedCount = 0;

                for (Url url : urls) {
                        Optional<UrlAnalysis> latestAnalysis = urlAnalysisRepository
                                        .findTopByUrl_UrlIdOrderByAnalyzedAtDesc(url.getUrlId());

                        if (latestAnalysis.isEmpty()) {
                                unanalyzedCount++;
                                continue;
                        }

                        RiskLevel riskLevel = latestAnalysis.get().getRiskLevel();

                        if (riskLevel == RiskLevel.CRITICAL) {
                                criticalCount++;
                        } else if (riskLevel == RiskLevel.DANGER) {
                                dangerCount++;
                        } else if (riskLevel == RiskLevel.WARNING) {
                                warningCount++;
                        } else if (riskLevel == RiskLevel.SAFE) {
                                safeCount++;
                        }
                }

                return new UrlStatisticsResponseDto(

                                totalCount,
                                criticalCount,
                                dangerCount,
                                warningCount,
                                safeCount,
                                unanalyzedCount);

        }

        // 나의 URL 목록 DTO 변환
        private MyUrlItemResponseDto toMyUrlItem(EmailUrl emailUrl) {

                Url url = emailUrl.getUrl();

                Optional<UrlAnalysis> latestAnalysis = urlAnalysisRepository
                                .findTopByUrl_UrlIdOrderByAnalyzedAtDesc(url.getUrlId());

                boolean analyzed = latestAnalysis.isPresent();

                String riskLevel = latestAnalysis
                                .map(analysis -> analysis.getRiskLevel().name())
                                .orElse("SAFE");

                String reasonSummary = latestAnalysis
                                .map(UrlAnalysis::getReasonSummary)
                                .orElse(null);

                return new MyUrlItemResponseDto(
                                url.getUrlId(),
                                emailUrl.getEmail().getEmailId(),
                                emailUrl.getEmail().getAccount().getAccountId(),
                                emailUrl.getEmail().getSenderName(),
                                emailUrl.getEmail().getSenderEmail(),
                                emailUrl.getEmail().getSubject(),
                                emailUrl.getRawUrl(),
                                url.getNormalizedUrl(),
                                url.getDomain(),
                                riskLevel,
                                reasonSummary,
                                analyzed,
                                emailUrl.getEmail().getReceivedAt(),
                                emailUrl.getCreatedAt());
        }

        private UrlListItemResponseDto toListItem(Url url) {
                Optional<UrlAnalysis> latestAnalysis = urlAnalysisRepository
                                .findTopByUrl_UrlIdOrderByAnalyzedAtDesc(url.getUrlId());

                boolean analyzed = latestAnalysis.isPresent();

                String riskLevel = latestAnalysis
                                .map(analysis -> analysis.getRiskLevel().name())
                                .orElse("SAFE");

                return new UrlListItemResponseDto(
                                url.getUrlId(),
                                url.getNormalizedUrl(),
                                url.getDomain(),
                                riskLevel,
                                analyzed,
                                url.getCreatedAt());
        }

        // 검색어 공백 처리
        private String normalizeSearchText(String value) {
                if (value == null || value.trim().isEmpty()) {
                        return null;
                }

                return value.trim();
        }

        // 위험도 문자열을 RiskLevel enum으로 변환
        private RiskLevel parseRiskLevel(String riskLevel) {
                if (riskLevel == null || riskLevel.trim().isEmpty()) {
                        return null;
                }

                String value = riskLevel.trim().toUpperCase();

                try {
                        return RiskLevel.valueOf(value);
                } catch (IllegalArgumentException e) {
                        throw new BusinessException(ErrorCode.INVALID_RISK_LEVEL, "올바르지 않은 위험도 값입니다.");
                }
        }

        // 통계 기간 조건 정규화
        private String normalizePeriod(String period) {
                if (period == null || period.trim().isEmpty()) {
                        return "ALL";
                }

                String value = period.trim().toUpperCase();

                if (!List.of("ALL", "TODAY", "WEEK", "MONTH").contains(value)) {
                        throw new BusinessException(ErrorCode.INVALID_PERIOD, "유효하지 않은 기간 조건입니다.");
                }

                return value;
        }

        // 통계 기간 조건에 따른 시작 시각 계산
        private LocalDateTime getStatisticsStartDateTime(String period) {
                LocalDateTime now = LocalDateTime.now();

                if ("TODAY".equals(period)) {
                        return now.toLocalDate().atStartOfDay();
                }

                if ("WEEK".equals(period)) {
                        return now.minusDays(7);
                }

                if ("MONTH".equals(period)) {
                        return now.minusMonths(1);
                }

                return null;
        }
}