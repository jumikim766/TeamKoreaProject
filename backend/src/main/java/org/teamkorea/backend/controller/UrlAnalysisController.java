package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.config.UserDetailsImpl;
import org.teamkorea.backend.domain.UrlAnalysis;
import org.teamkorea.backend.dto.*;
import org.teamkorea.backend.service.AnalysisService;
import org.teamkorea.backend.service.NotificationService;

import org.teamkorea.backend.ai.LlmAnalysisService;
import org.teamkorea.backend.ai.dto.LlmAnalysisResponse;
import org.teamkorea.backend.domain.RiskLevel;
import org.teamkorea.backend.domain.Url;
import org.teamkorea.backend.repository.UrlAnalysisRepository;
import org.teamkorea.backend.repository.UrlRepository;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/url-analysis")
@RequiredArgsConstructor
public class UrlAnalysisController {

    private final AnalysisService analysisService;
    private final NotificationService notificationService; // 알림 서비스 주입
    private final UrlRepository urlRepository;
    private final UrlAnalysisRepository urlAnalysisRepository;
    private final LlmAnalysisService llmAnalysisService;
    /**
     * 1. URL 분석 실행
     */
    @PostMapping("/analyze")
    public ResponseEntity<BaseResponse<UrlAnalysis>> analyzeUrl(
            @RequestParam Long userId,
            @RequestParam Long urlId
    ) {
        UrlAnalysis result = analysisService.analyzeAndSave(userId, urlId);
        return ResponseEntity.ok(BaseResponse.success("URL 분석이 완료되었습니다.", result));
    }

    /**
     * 2. 내 분석 히스토리 목록 조회
     */
    @GetMapping("/history")
    public ResponseEntity<BaseResponse<Page<AnalysisHistoryResponseDto>>> getAnalysisList(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AnalysisHistoryResponseDto> response = analysisService.getAnalysisList(userDetails.getUser(), pageable);
        return ResponseEntity.ok(BaseResponse.success("분석 결과 목록 조회에 성공했습니다.", response));
    }

    /**
     * 3. 분석 결과 상세 조회
     */
    @GetMapping("/{analysisId}")
    public ResponseEntity<BaseResponse<AnalysisDetailResponseDto>> getAnalysisDetail(
            @PathVariable Long analysisId
    ) {
        AnalysisDetailResponseDto detail = analysisService.getDetail(analysisId);
        return ResponseEntity.ok(BaseResponse.success("분석 결과 상세 조회에 성공했습니다.", detail));
    }

    /**
     * 4. 알림 목록 조회 (페이징 + 안읽은 알림 필터 조건 보완)
     * API 명세: GET /api/url-analysis/notifications?unreadOnly=true
     */
    @GetMapping("/notifications")
    public ResponseEntity<BaseResponse<Page<NotificationResponseDto>>> getNotifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<NotificationResponseDto> response = 
                notificationService.getNotifications(userDetails.getUser(), unreadOnly, pageable);
        return ResponseEntity.ok(BaseResponse.success("알림 목록 조회에 성공했습니다.", response));
    }

    /**
     * 5. 알림 읽음 단건 처리 (신규 완료 단계 추가 - FNC-063)
     * API 명세: PATCH /api/url-analysis/notifications/{notificationId}/read
     */
    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<BaseResponse<NotificationResponseDto>> markNotificationAsRead(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long notificationId
    ) {
        NotificationResponseDto response = notificationService.markAsRead(notificationId, userDetails.getUser());
        return ResponseEntity.ok(BaseResponse.success("알림 읽음 처리가 완료되었습니다.", response));
    }
    @GetMapping("/llm/url/{urlId}")
public ResponseEntity<BaseResponse<LlmAnalysisResponse>> analyzeUrlWithLlm(
        @PathVariable Long urlId
) {

     System.out.println("===== LLM URL 분석 API 진입 ===== urlId = " + urlId);

    Url url = urlRepository.findById(urlId)
            .orElseThrow(() ->
                    new IllegalArgumentException("해당 URL을 찾을 수 없습니다. urlId=" + urlId));

    LlmAnalysisResponse response = llmAnalysisService.analyze(
            url.getNormalizedUrl(),
            url.getDomain(),
            null,
            0.0,
            false,
            false
    );

   UrlAnalysis analysis = UrlAnalysis.builder()
        .url(url)
        .domain(
                url.getDomain() != null
                        ? url.getDomain()
                        : "unknown-domain"
        )
        .sourceType("LLM")
        .riskLevel(convertRiskLevel(response.getRisk()))
        .riskType("PHISHING")
        .score(BigDecimal.valueOf(response.getScore()))
        .reasonSummary(response.getReasonSummary())
        .featuresJson(
        "{\"rules\": \"" +
                String.join(", ", response.getDetectedRules())
                + "\"}"
)
        .build();

    urlAnalysisRepository.save(analysis);

    return ResponseEntity.ok(
            BaseResponse.success(
                    "LLM 분석 및 저장이 완료되었습니다.",
                    response
            )
    );
}

private RiskLevel convertRiskLevel(String risk) {

    if ("DANGER".equals(risk)) {
        return RiskLevel.DANGER;
    }

    if ("WARNING".equals(risk)) {
        return RiskLevel.WARNING;
    }

    return RiskLevel.SAFE;
}
}