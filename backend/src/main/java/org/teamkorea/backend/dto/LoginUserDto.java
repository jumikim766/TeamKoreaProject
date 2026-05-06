package org.teamkorea.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserDto {

    private Long userId;
    private String email;
    private String name;
    private String role;
}