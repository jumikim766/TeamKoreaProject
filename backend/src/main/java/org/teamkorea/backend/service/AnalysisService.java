package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.*;
import org.teamkorea.backend.dto.AnalysisDetailResponseDto;
import org.teamkorea.backend.dto.AnalysisListPageResponseDto;
import org.teamkorea.backend.dto.AnalysisListResponseDto;
import org.teamkorea.backend.repository.*;
import org.teamkorea.backend.ai.LlmAnalysisService;
import org.teamkorea.backend.ai.dto.LlmAnalysisResponse;
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
    private final DomainReputationRepository reputationRepository;
    private final LlmAnalysisService llmAnalysisService;

    public UrlAnalysis analyzeAndSave(Long userId, Long urlId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new IllegalArgumentException("URL을 찾을 수 없습니다."));

        DomainReputation reputation = reputationRepository.findByDomain(url.getDomain())
                .orElse(DomainReputation.builder()
                        .domain(url.getDomain())
                        .trustScore(50)
                        .build());

        // 1. 기술 점수 계산
        double technicalScore = calculateTechnicalScore(url.getNormalizedUrl());
        
        // 2. 최종 점수 계산 (평판 도메인에서 수정했던 getTrustScoreValue() 사용)
        double finalScore = (technicalScore * 0.7) + (reputation.getTrustScoreValue() * 0.3);

        // 3. Enum 타입으로 등급 결정
        RiskLevel riskLevel = determineRiskLevel(finalScore, reputation);
        BigDecimal scoreValue = BigDecimal.valueOf(finalScore);

        UrlAnalysis newAnalysis = UrlAnalysis.builder()
                .url(url)
                .sourceType("SYSTEM")
                .riskLevel(riskLevel) // Enum 타입 적용
                .riskType(RiskLevel.SAFE.equals(riskLevel) ? null : "PHISHING")
                .score(scoreValue)
                .sslVerified(false)
                .redirectionDepth(0)
                .containsFormInput(false)
                .reasonSummary(
                        generateLlmSummary(url, riskLevel, finalScore, reputation))
                .ruleVersion("v1.1")
                .analyzedAt(LocalDateTime.now())
                .build();

        UrlAnalysis saved = urlAnalysisRepository.save(newAnalysis);
        analysisHistoryRepository.save(new AnalysisHistory(user, saved, "WEB"));
        return saved;
    }

    @Transactional(readOnly = true)
    public AnalysisListPageResponseDto getAnalysisList(String riskLevel, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "analyzedAt"));
        Page<UrlAnalysis> analysisPage;

        if (riskLevel != null && !riskLevel.isBlank()) {
            // String을 Enum으로 변환하여 조회
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

    @Transactional(readOnly = true)
    public AnalysisDetailResponseDto getDetail(Long analysisId) {
        UrlAnalysis analysis = urlAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("해당 분석 결과를 찾을 수 없습니다."));
        return new AnalysisDetailResponseDto(analysis);
    }

    // --- 내부 로직 메서드 ---

    private RiskLevel determineRiskLevel(double score, DomainReputation rep) {
        // 블랙리스트 여부 확인 및 점수별 Enum 반환
        if (Boolean.TRUE.equals(rep.getIsBlacklisted()) || score < 40) return RiskLevel.CRITICAL;
        if (score < 60) return RiskLevel.DANGER;
        if (score < 80) return RiskLevel.WARNING;
        return RiskLevel.SAFE;
    }

    private double calculateTechnicalScore(String url) {
        if (url == null || url.isBlank()) return 30.0;
        if (url.contains("@") || url.matches(".*\\d{1,3}\\.\\d{1,3}.*")) return 20.0;
        if (url.length() > 100) return 50.0;
        return 90.0; 
    }

    private String generateSummary(RiskLevel riskLevel, DomainReputation rep) {
        if (Boolean.TRUE.equals(rep.getIsBlacklisted())) return "블랙리스트에 등록된 위험 도메인입니다.";
        return "종합 분석 결과 " + riskLevel.name() + " 등급으로 판정되었습니다.";
    }
    private String generateLlmSummary(Url url, RiskLevel riskLevel, double finalScore, DomainReputation reputation) {
    try {
        LlmAnalysisResponse llmResponse = llmAnalysisService.analyze(
                url.getNormalizedUrl(),
                url.getDomain(),
                riskLevel.name(),
                finalScore,
                Boolean.TRUE.equals(reputation.getIsBlacklisted()),
                Boolean.TRUE.equals(reputation.getIsWhitelisted())
        );

        if (llmResponse != null && llmResponse.getReasonSummary() != null && !llmResponse.getReasonSummary().isBlank()) {
            return llmResponse.getReasonSummary();
        }
    } catch (Exception e) {
        return generateSummary(riskLevel, reputation);
    }

    return generateSummary(riskLevel, reputation);
}

}