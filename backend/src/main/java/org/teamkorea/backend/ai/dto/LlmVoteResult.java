package org.teamkorea.backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LlmVoteResult {
    private String provider;
    private String risk;
    private double confidence;
    private String reason;
    private boolean success;

    public static LlmVoteResult fallback(String provider, String risk, String reason) {
        return new LlmVoteResult(provider, risk, 0.0, reason, false);
    }
}