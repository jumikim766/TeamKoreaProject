package org.teamkorea.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserMeResponseDto {

    private Long userId;
    private String email;
    private String username;
    private String name;
    private String role;
    private String provider;
    private String gender;
    private Integer age;
    private String status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}