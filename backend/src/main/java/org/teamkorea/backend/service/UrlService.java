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

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UrlService {

    private final UrlRepository urlRepository;
    private final UrlAnalysisRepository urlAnalysisRepository;

    public UrlListResponseDto getUrls(
            String domain,
            String riskLevel,
            Boolean isAnalyzed,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Url> urlPage;

        if (domain != null && !domain.isBlank()) {
            urlPage = urlRepository.findByDomainContainingIgnoreCase(domain, pageable);
        } else {
            urlPage = urlRepository.findAll(pageable);
        }

        List<UrlListItemResponseDto> urls = urlPage.getContent().stream()
                .map(this::toListItem)
                .filter(dto -> riskLevel == null || riskLevel.isBlank()
                        || riskLevel.equals(dto.getRiskLevel()))
                .filter(dto -> isAnalyzed == null
                        || isAnalyzed.equals(dto.getIsAnalyzed()))
                .toList();

        return new UrlListResponseDto(
                urls,
                page,
                size,
                urlPage.getTotalElements(),
                urlPage.getTotalPages()
        );
    }

    public UrlDetailResponseDto getUrlDetail(Long urlId) {
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new IllegalArgumentException("해당 URL 정보를 찾을 수 없습니다."));

        Optional<UrlAnalysis> latestAnalysis =
                urlAnalysisRepository.findTopByUrl_UrlIdOrderByAnalyzedAtDesc(urlId);

        String riskLevel = latestAnalysis
                .map(analysis -> analysis.getRiskLevel().name())
                .orElse("UNKNOWN");

        String reasonSummary = latestAnalysis
                .map(UrlAnalysis::getReasonSummary)
                .orElse(null);

        return new UrlDetailResponseDto(
                url.getUrlId(),
                url.getNormalizedUrl(),   // originalUrl 임시 처리
                url.getNormalizedUrl(),
                url.getDomain(),
                riskLevel,
                reasonSummary,
                url.getCreatedAt(),
                url.getLastSeenAt()       // updatedAt 대신 lastSeenAt 사용
        );
    }

    private UrlListItemResponseDto toListItem(Url url) {
        Optional<UrlAnalysis> latestAnalysis =
                urlAnalysisRepository.findTopByUrl_UrlIdOrderByAnalyzedAtDesc(url.getUrlId());

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
                url.getCreatedAt()
        );
    }
}