package org.teamkorea.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor // Lombok을 적용하여 수동 Getter/Setter 제거
public class SignupRequestDto {

    @NotBlank(message = "아이디는 필수입니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9]{4,20}$", message = "아이디는 영문을 1자 이상 포함한 4~20자의 영문 또는 영문+숫자 조합이어야 합니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @ValidPassword
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    @Pattern(regexp = "^(?!\\s)(?!.*\\s$).{1,30}$", message = "이름은 앞뒤 공백 없이 1자 이상 30자 이하로 입력해주세요.")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "이메일 인증번호는 필수입니다.")
    private String code;

    // null은 통과하고, 빈 문자열("") 또는 입력값이 있을 경우에는 010으로 시작하는 11자리만 허용합니다.
    @Pattern(regexp = "^$|^010\\d{8}$", message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다.")
    private String phone;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)?$", message = "성별은 MALE, FEMALE, OTHER 중 하나여야 합니다.")
    private String gender;

    @Min(value = 1, message = "나이는 1 이상이어야 합니다.")
    @Max(value = 120, message = "나이는 120 이하여야 합니다.")
    private Integer age;
}