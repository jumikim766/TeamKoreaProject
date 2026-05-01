package org.teamkorea.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder // 테스트나 내부 생성 시 유용 (유지해도 OK)
public class EmailAccountRequestDto {

    @NotBlank(message = "서비스 제공자(Gmail, Naver 등)는 필수 입력값입니다.")
    private String provider;

    @Email(message = "유효한 이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일 주소는 필수 입력값입니다.")
    private String email;

    @NotBlank(message = "IMAP 호스트 주소는 필수 입력값입니다.")
    private String imapHost;

    @NotNull(message = "IMAP 포트 번호는 필수 입력값입니다.")
    private Integer imapPort;

    @NotBlank(message = "메일 서버 로그인 아이디는 필수 입력값입니다.")
    private String loginId;

    @NotBlank(message = "메일 서버 비밀번호(또는 앱 비밀번호)는 필수 입력값입니다.")
    private String password;

    /**
     * 주의:
     * password는 평문으로 들어오며,
     * Service 계층에서 반드시 암호화 후 secretEnc로 변환해야 합니다.
     */
}