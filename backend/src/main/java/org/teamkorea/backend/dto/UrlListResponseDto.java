package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UrlListResponseDto {

    private List<UrlListItemResponseDto> urls;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}