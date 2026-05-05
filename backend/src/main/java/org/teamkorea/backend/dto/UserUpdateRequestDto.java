package org.teamkorea.backend.dto;

import lombok.Getter;
import jakarta.validation.constraints.Pattern;

@Getter
public class UserUpdateRequestDto {

    private String name;
        // ===== 추가: 전화번호 형식 검증 =====
    @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다.")
    private String phone;
    private String gender;
    private Integer age;

}