package org.teamkorea.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailAccountRequestDto {

    @NotBlank
    private String provider;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String imapHost;

    @NotNull
    private Integer imapPort;

    @NotBlank
    private String loginId;

    @NotBlank
    private String password;
}