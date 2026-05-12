package org.teamkorea.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpdateResponseDto {

    private Long userId;
    private String name;
    private String gender;
    private Integer age;
    private String phoneMasked;
    private String username;
    private String email;
}