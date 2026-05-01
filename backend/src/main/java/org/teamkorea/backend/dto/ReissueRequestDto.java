package org.teamkorea.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReissueRequestDto {

    @NotBlank(message = "재발급을 위한 Refresh Token은 필수입니다.")
    private String refreshToken;
}