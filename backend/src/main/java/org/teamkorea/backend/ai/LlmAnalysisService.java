package org.teamkorea.backend.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.teamkorea.backend.ai.dto.LlmAnalysisResponse;

@Service
@RequiredArgsConstructor
public class LlmAnalysisService {

    private final LlmClient llmClient;

    public LlmAnalysisResponse analyze(
            String url,
            String domain,
            String riskLevel,
            double score,
            boolean isBlacklisted,
            boolean isWhitelisted
    ) {

        String prompt = "URL: " + url + "\n위험도 판단해줘";

        String result = llmClient.call(prompt);

        return new LlmAnalysisResponse("WARNING", result);
    }
}