package org.teamkorea.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class UserUpdateRequestDto {

    @Pattern(regexp = "^(?!\\s)(?!.*\\s$).{1,30}$", message = "이름은 앞뒤 공백 없이 1자 이상 30자 이하로 입력해주세요.")
    private String name;

    @Pattern(regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9]{4,20}$", message = "아이디는 영문 또는 영문+숫자 조합의 4~20자여야 합니다.")
    private String username;

    @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다.")
    private String phone;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)?$", message = "성별은 MALE, FEMALE, OTHER 중 하나여야 합니다.")
    private String gender;

    @Min(value = 1, message = "나이는 1 이상이어야 합니다.")
    @Max(value = 120, message = "나이는 120 이하여야 합니다.")
    private Integer age;
}