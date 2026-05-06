package org.teamkorea.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder // 테스트 및 서비스 레이어 편의성을 위해 추가
@NoArgsConstructor
@AllArgsConstructor // Builder 사용 시 필수
public class ReportRequestDto {

    private Long urlId; // 선택 사항 (기존 분석 결과가 있는 경우)

    @NotBlank(message = "신고할 URL 주소는 필수입니다.") // 유효성 검사 추가
    private String reportedUrl;

    @NotBlank(message = "신고 사유를 입력해주세요.") // 유효성 검사 추가
    private String reason;
}