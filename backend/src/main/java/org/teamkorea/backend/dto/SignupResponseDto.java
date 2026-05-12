package org.teamkorea.backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponseDto {

    private Long userId;
    private String username;
    private String name;
    private String email;
    private String role;
    private String status;
    private LocalDateTime createdAt;
}