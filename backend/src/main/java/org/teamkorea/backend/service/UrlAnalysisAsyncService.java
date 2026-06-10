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
public void analyzeUrlAsync(
        Long userId,
        Long urlId,
        String emailSubject,
        String emailBody
) {

    System.out.println(
            "===== URL 분석 시작 ===== urlId=" + urlId
    );

    try {

        analysisService.analyzeWithLlmAndSave(
                userId,
                urlId,
                emailSubject,
                emailBody
        );

        System.out.println(
                "===== URL 분석 성공 ===== urlId=" + urlId
        );

    } catch (Exception e) {

        e.printStackTrace();

        System.out.println(
                "===== URL 분석 실패 ===== "
                        + e.getMessage()
        );

        log.warn(
                "[URL ANALYSIS ASYNC] URL 분석 실패 - userId={}, urlId={}, reason={}",
                userId,
                urlId,
                e.getMessage()
        );
    }
}
}