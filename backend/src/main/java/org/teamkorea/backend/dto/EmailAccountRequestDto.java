package org.teamkorea.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailAccountRequestDto {

    @NotBlank(message = "provider는 필수입니다.")
    private String provider;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "email은 필수입니다.")
    private String email;

    @NotBlank(message = "loginId는 필수입니다.")
    private String loginId;

    @NotBlank(message = "secret은 필수입니다.")
    private String secret;

    private String imapHost;

    private Integer imapPort;
}