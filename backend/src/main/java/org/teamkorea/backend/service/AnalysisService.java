package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.*;
import org.teamkorea.backend.repository.*;

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

    public UrlAnalysis analyzeAndSave(Long userId, Long urlId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new IllegalArgumentException("URL을 찾을 수 없습니다."));

        // 1. 도메인 평판 정보 조회
        DomainReputation reputation = reputationRepository.findByDomain(url.getDomain())
                .orElse(DomainReputation.builder()
                        .domain(url.getDomain())
                        .trustScore(50)
                        .build());

        // 2. 기술적 분석 점수 계산
        double technicalScore = calculateTechnicalScore(url.getNormalizedUrl());

        // 3. 최종 점수 계산 (기술 70% + 평판 30%)
        double finalScore = (technicalScore * 0.7) + (reputation.getTrustScore() * 0.3);

        // 4. 최종 등급 결정 (명세서 기준: SAFE, WARNING, DANGER, CRITICAL)
        String riskLevel = determineRiskLevel(finalScore, reputation);
        BigDecimal scoreValue = BigDecimal.valueOf(finalScore);

        // 5. 분석 결과 객체 생성 및 저장
        UrlAnalysis newAnalysis = UrlAnalysis.builder()
                .url(url)
                .sourceType("SYSTEM")
                .riskLevel(riskLevel)
                .riskType(riskLevel.equals("SAFE") ? null : "PHISHING")
                .score(scoreValue)
                .sslVerified(false)
                .redirectionDepth(0)
                .containsFormInput(false)
                .reasonSummary(generateSummary(riskLevel, reputation))
                .ruleVersion("v1.1")
                .analyzedAt(LocalDateTime.now())
                .build();

        UrlAnalysis saved = urlAnalysisRepository.save(newAnalysis);
        
        // 6. 히스토리 저장
        analysisHistoryRepository.save(new AnalysisHistory(user, saved, "WEB"));

        return saved;
    }

    private String determineRiskLevel(double score, DomainReputation rep) {
        if (rep.getIsBlacklisted() || score < 40) return "CRITICAL";
        if (score < 60) return "DANGER";
        if (score < 80) return "WARNING";
        return "SAFE";
    }

    private double calculateTechnicalScore(String url) {
        if (url == null || url.isBlank()) return 30.0;
        if (url.contains("@") || url.matches(".*\\d{1,3}\\.\\d{1,3}.*")) return 20.0;
        if (url.length() > 100) return 50.0;
        return 90.0; 
    }

    private String generateSummary(String riskLevel, DomainReputation rep) {
        if (rep.getIsBlacklisted()) return "블랙리스트에 등록된 위험 도메인입니다.";
        return "종합 분석 결과 " + riskLevel + " 등급으로 판정되었습니다.";
    }

    @Transactional(readOnly = true)
    public List<UrlAnalysis> getAllAnalyses() {
        return urlAnalysisRepository.findAll();
    }

    @Transactional(readOnly = true)
    public UrlAnalysis getAnalysisById(Long analysisId) {
        return urlAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("해당 분석 결과를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<UrlAnalysis> getAnalysesByRiskLevel(String riskLevel) {
        // 기존 미구현 메서드 완성
        return urlAnalysisRepository.findByRiskLevel(riskLevel);
    }
}