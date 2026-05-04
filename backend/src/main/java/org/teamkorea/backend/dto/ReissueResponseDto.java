package org.teamkorea.backend.dto;

public class ReissueResponseDto {

    private String accessToken;
    private String tokenType;
    private String message;

    public ReissueResponseDto() {
    }

    public ReissueResponseDto(String accessToken, String tokenType, String message) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.message = message;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getMessage() {
        return message;
    }
}