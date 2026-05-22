package org.teamkorea.backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LlmAnalysisResponse {

    private String risk;
    private String reasonSummary;
    private double score;
    private List<String> detectedRules;
}