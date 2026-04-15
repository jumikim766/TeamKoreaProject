package org.teamkorea.backend.dto;

public class LoginResponseDto {

    private Long userId;
    private String username;
    private String name;
    private String email;
    private String accessToken;
    private String message;

    public LoginResponseDto() {
    }

    public LoginResponseDto(Long userId, String username, String name, String email, String accessToken, String message) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.email = email;
        this.accessToken = accessToken;
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

    public String getMessage() {
        return message;
    }
}