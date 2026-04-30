package org.teamkorea.backend.dto;

public class ReissueRequestDto {

    private String refreshToken;

    public ReissueRequestDto() {
    }

    public ReissueRequestDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}