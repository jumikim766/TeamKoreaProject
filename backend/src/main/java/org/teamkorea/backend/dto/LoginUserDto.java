package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginUserDto {
    private Long userId;
    private String email;
    private String name;
    private String role;
}