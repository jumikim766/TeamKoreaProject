package org.teamkorea.backend.dto;

public class LoginResponseDto {

    private Long userId;
    private String username;
    private String name;
    private String email;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String message;

    public LoginResponseDto() {
    }

    public LoginResponseDto(
            Long userId,
            String username,
            String name,
            String email,
            String accessToken,
            String refreshToken,
            String tokenType,
            String message
    ) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.email = email;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getMessage() {
        return message;
    }
}