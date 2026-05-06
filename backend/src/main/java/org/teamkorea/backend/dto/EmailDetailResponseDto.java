package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class EmailDetailResponseDto {

    private Long emailId;
    private Long accountId;
    private String senderEmail;
    private String senderName;
    private String receiverEmail;
    private String subject;
    private String bodyText;
    private LocalDateTime receivedAt;
    private LocalDateTime createdAt;
    private int urlCount;
}