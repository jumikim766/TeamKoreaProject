package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisListPageResponseDto {

    private List<AnalysisListResponseDto> analyses;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}