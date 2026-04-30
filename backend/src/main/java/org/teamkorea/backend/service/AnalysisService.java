package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.AnalysisHistory;
import org.teamkorea.backend.domain.Url;
import org.teamkorea.backend.domain.UrlAnalysis;
import org.teamkorea.backend.domain.User;
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

        String riskLevel = checkRules(url.getNormalizedUrl());
        String riskType = null;
        BigDecimal score = "SAFE".equals(riskLevel)
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

    @Transactional(readOnly = true)
    public List<UrlAnalysis> getAllAnalyses() {
        return urlAnalysisRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<UrlAnalysis> getAnalysesByRiskLevel(String riskLevel) {
        return urlAnalysisRepository.findByRiskLevel(riskLevel);
    }

    @Transactional(readOnly = true)
    public UrlAnalysis getAnalysisById(Long analysisId) {
        return urlAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("해당 분석 결과를 찾을 수 없습니다."));
    }

    private String checkRules(String url) {
        if (url == null || url.isBlank()) {
            return "SUSPICIOUS";
        }
        if (url.length() > 100) return "SUSPICIOUS";
        if (url.contains("@")) return "MALICIOUS";
        if (url.matches(".*\\d{1,3}\\.\\d{1,3}.*")) return "MALICIOUS";
        return "SAFE";
    }
}