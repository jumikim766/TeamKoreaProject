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
import org.teamkorea.backend.repository.EmailUrlRepository;

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

                // 빈 문자열 domain은 검색 조건에서 제외
                String searchDomain = null;
                if (domain != null && !domain.trim().isEmpty()) {
                        searchDomain = domain.trim();
                }

                // 문자열 reiskLevel을 enum으로 변환
                RiskLevel searchRiskLevel = null;
                if (riskLevel != null && !riskLevel.trim().isEmpty()) {
                        try {
                                searchRiskLevel = RiskLevel.valueOf(riskLevel.trim().toUpperCase());
                        } catch (IllegalArgumentException e) {
                                throw new IllegalArgumentException("올바르지 않은 위험도 값입니다.");
                        }
                }

                // URL 목록 조회
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

        // URL 상세 조회
        public UrlDetailResponseDto getUrlDetail(Long urlId) {
                Url url = urlRepository.findById(urlId)
                                .orElseThrow(() -> new IllegalArgumentException("해당 URL 정보를 찾을 수 없습니다."));

                Optional<UrlAnalysis> latestAnalysis = urlAnalysisRepository
                                .findTopByUrl_UrlIdOrderByAnalyzedAtDesc(urlId);

                String riskLevel = latestAnalysis
                                .map(analysis -> analysis.getRiskLevel().name())
                                .orElse("UNKNOWN");

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

                return new UrlDetailResponseDto(
                                url.getUrlId(),
                                senderName,
                                senderEmail,
                                originalUrl,
                                url.getNormalizedUrl(),
                                url.getDomain(),
                                riskLevel,
                                reasonSummary,
                                url.getCreatedAt(),
                                url.getLastSeenAt()
                );
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

        // URL 위험도 통계 조회
        public UrlStatisticsResponseDto getUrlStatistics(
                        Long userId,
                        String scope,
                        Long accountId,
                        String domain,
                        Boolean isAnalyzed) {
                String searchDomain = normalizeSearchText(domain);

                List<Url> urls;

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

                long totalCount = urls.size();
                long criticalCount = 0;
                long dangerCount = 0;
                long cautionCount = 0;
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
                        } else if (riskLevel == RiskLevel.WARNING || riskLevel == RiskLevel.SUSPICIOUS) {
                                cautionCount++;
                        } else if (riskLevel == RiskLevel.SAFE) {
                                safeCount++;
                        }
                }

                return new UrlStatisticsResponseDto(
                                totalCount,
                                criticalCount,
                                dangerCount,
                                cautionCount,
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
                                .orElse("UNKNOWN");

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
                                .orElse("UNKNOWN");

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

        // API 명세서 위험도 값과 현재 RiskLevel enum 값 매핑
        private RiskLevel parseRiskLevel(String riskLevel) {
                if (riskLevel == null || riskLevel.trim().isEmpty()) {
                        return null;
                }

                String value = riskLevel.trim().toUpperCase();

                if ("DANGEROUS".equals(value)) {
                        value = "DANGER";
                }

                if ("CAUTION".equals(value)) {
                        value = "WARNING";
                }

                try {
                        return RiskLevel.valueOf(value);
                } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("올바르지 않은 위험도 값입니다.");
                }
        }
}