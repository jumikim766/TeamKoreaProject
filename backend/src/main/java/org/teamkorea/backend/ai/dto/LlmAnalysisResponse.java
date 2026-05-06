package org.teamkorea.backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LlmAnalysisResponse {

    private String risk;
    private String reasonSummary;
}