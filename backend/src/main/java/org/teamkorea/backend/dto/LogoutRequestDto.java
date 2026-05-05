package org.teamkorea.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LogoutRequestDto {

    // 로그아웃 시 무효화할 Refresh Token
    @NotBlank(message = "refreshToken은 필수입니다.")
    private String refreshToken;
}