package org.teamkorea.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class PasswordResetRequestDto {

    @NotBlank(message = "아이디는 필수입니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9]{4,20}$", message = "아이디는 영문을 1자 이상 포함한 4~20자의 영문 또는 영문+숫자 조합이어야 합니다.")
    private String username;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "인증번호는 필수입니다.")
    private String code;

    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @ValidPassword
    private String newPassword;
}