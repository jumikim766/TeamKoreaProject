package org.teamkorea.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequestDto {

    @NotBlank(message = "신고할 URL은 필수 입력 항목입니다.")
    private String url;

    @NotBlank(message = "신고 사유를 입력해 주세요.")
    private String reason;

    public String getReportedUrl() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getReportedUrl'");
    }

    public Object getUrlId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUrlId'");
    }
}