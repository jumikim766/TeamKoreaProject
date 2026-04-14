package org.teamkorea.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailAccountRequestDto {

    private Long userId;

    private String email;
    private String provider;

    private String imapHost;
    private Integer imapPort;

    private String loginId;
    private String password; // 평문 (나중에 암호화)
}