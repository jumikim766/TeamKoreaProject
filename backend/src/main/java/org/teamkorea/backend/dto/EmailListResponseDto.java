package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class EmailListResponseDto {

    private Long emailId;
    private String senderName;
    private String senderEmail;
    private String subject;
    private String previewText;
    private LocalDateTime receivedAt;
}