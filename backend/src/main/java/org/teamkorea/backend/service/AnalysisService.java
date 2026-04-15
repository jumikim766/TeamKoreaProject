package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.UrlAnalysis;
import org.teamkorea.backend.domain.AnalysisHistory;
import org.teamkorea.backend.repository.UrlAnalysisRepository;
import org.teamkorea.backend.repository.AnalysisHistoryRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class AnalysisService {
    private final UrlAnalysisRepository urlAnalysisRepository;
    private final AnalysisHistoryRepository historyRepository;

    public UrlAnalysis analyzeAndSave(String url, String userEmail) {
        // 1. 기존 분석 데이터가 있는지 확인 (캐싱)
        return urlAnalysisRepository.findByOriginalUrl(url)
                .map(analysis -> {
                    // 이미 있다면 히스토리만 추가 저장
                    historyRepository.save(new AnalysisHistory(analysis, userEmail));
                    return analysis;
                })
                .orElseGet(() -> {
                    // 2. 없으면 규칙 기반 분석 실행 (checkRules 호출)
                    String level = checkRules(url);
                    Double score = level.equals("SAFE") ? 0.0 : 70.0; // 규칙 위반 시 70점 부여 예시

                    UrlAnalysis newAnalysis = UrlAnalysis.builder()
                            .originalUrl(url)
                            .riskScore(score)
                            .riskLevel(level)
                            .build();
                    
                    UrlAnalysis saved = urlAnalysisRepository.save(newAnalysis);
                    
                    // 3. 사용자 히스토리에 기록
                    historyRepository.save(new AnalysisHistory(saved, userEmail));
                    return saved;
                });
    }

    // [이 위치에 추가!] 규칙 기반 분석 로직
    private String checkRules(String url) {
        if (url.length() > 100) return "SUSPICIOUS"; // URL이 너무 김
        if (url.contains("@")) return "MALICIOUS";   // 사용자 정보 탈취 시도 기호
        if (url.matches(".*\\d{1,3}\\.\\d{1,3}.*")) return "MALICIOUS"; // IP 주소 형태의 URL 포함
        return "SAFE";
    }
}