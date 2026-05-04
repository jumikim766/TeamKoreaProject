package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserMeResponseDto {

    private Long userId;
    private String email;
    private String username;
    private String name;
    private String role;
    private String provider;
}