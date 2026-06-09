package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlAnalysisAsyncService {

    private final AnalysisService analysisService;

    @Async
    public void analyzeUrlAsync(Long userId, Long urlId, String emailSubject, String emailBody) {
        try {
            // 현재 AnalysisService에 존재하는 규칙 기반 URL 분석 메서드 호출
            analysisService.analyzeAndSave(userId, urlId);

        } catch (Exception e) {
            // URL 분석 실패가 이메일 저장 실패로 이어지면 안 됨
            log.warn("[URL ANALYSIS ASYNC] URL 분석 실패 - userId={}, urlId={}, reason={}",
                    userId, urlId, e.getMessage());
        }
    }
}