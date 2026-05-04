package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.*;
import org.teamkorea.backend.dto.AnalysisDetailResponseDto;
import org.teamkorea.backend.dto.AnalysisListPageResponseDto;
import org.teamkorea.backend.dto.AnalysisListResponseDto;
import org.teamkorea.backend.repository.AnalysisHistoryRepository;
import org.teamkorea.backend.repository.UrlAnalysisRepository;
import org.teamkorea.backend.repository.UrlRepository;
import org.teamkorea.backend.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AnalysisService {

    private final UrlRepository urlRepository;
    private final UrlAnalysisRepository urlAnalysisRepository;
    private final AnalysisHistoryRepository analysisHistoryRepository;
    private final UserRepository userRepository;

    public UrlAnalysis analyzeAndSave(Long userId, Long urlId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new IllegalArgumentException("URL을 찾을 수 없습니다."));

        UrlAnalysis latest = urlAnalysisRepository
                .findTopByUrl_UrlIdOrderByAnalyzedAtDesc(urlId)
                .orElse(null);

        if (latest != null) {
            analysisHistoryRepository.save(new AnalysisHistory(user, latest, "WEB"));
            return latest;
        }

        // String이 아니라 RiskLevel enum 사용
        RiskLevel riskLevel = checkRules(url.getNormalizedUrl());

        String riskType = null;

        BigDecimal score = RiskLevel.SAFE.equals(riskLevel)
                ? BigDecimal.ZERO
                : new BigDecimal("70.00");

        UrlAnalysis newAnalysis = new UrlAnalysis(
                url,
                "RULE",
                riskLevel,
                riskType,
                score,
                false,
                0,
                false,
                "기본 규칙 검사 결과",
                null,
                "v1",
                LocalDateTime.now()
        );

        UrlAnalysis saved = urlAnalysisRepository.save(newAnalysis);
        analysisHistoryRepository.save(new AnalysisHistory(user, saved, "WEB"));

        return saved;
    }

    /**
     * 분석 결과 목록 조회
     * API 명세서 기준: analyses, page, size, totalElements, totalPages 포함
     */
    @Transactional(readOnly = true)
    public AnalysisListPageResponseDto getAnalysisList(String riskLevel, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "analyzedAt"));

        Page<UrlAnalysis> analysisPage;

        if (riskLevel != null && !riskLevel.isBlank()) {
            RiskLevel level = RiskLevel.valueOf(riskLevel);
            analysisPage = urlAnalysisRepository.findByRiskLevel(level, pageable);
        } else {
            analysisPage = urlAnalysisRepository.findAll(pageable);
        }

        List<AnalysisListResponseDto> analyses = analysisPage.getContent()
                .stream()
                .map(AnalysisListResponseDto::new)
                .toList();

        return AnalysisListPageResponseDto.builder()
                .analyses(analyses)
                .page(analysisPage.getNumber())
                .size(analysisPage.getSize())
                .totalElements(analysisPage.getTotalElements())
                .totalPages(analysisPage.getTotalPages())
                .build();
    }

    /**
     * 분석 결과 상세 조회
     */
    @Transactional(readOnly = true)
    public AnalysisDetailResponseDto getDetail(Long analysisId) {
        UrlAnalysis analysis = urlAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("해당 분석 결과를 찾을 수 없습니다."));

        return new AnalysisDetailResponseDto(analysis);
    }

    /**
     * URL 기본 규칙 검사
     */
    private RiskLevel checkRules(String url) {
        if (url == null || url.isBlank()) {
            return RiskLevel.SUSPICIOUS;
        }

        if (url.length() > 100) {
            return RiskLevel.SUSPICIOUS;
        }

        // MALICIOUS는 명세 enum에 없으므로 DANGEROUS로 처리
        if (url.contains("@")) {
            return RiskLevel.DANGEROUS;
        }

        if (url.matches(".*\\d{1,3}\\.\\d{1,3}.*")) {
            return RiskLevel.DANGEROUS;
        }

        return RiskLevel.SAFE;
    }
}