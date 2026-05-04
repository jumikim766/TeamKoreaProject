package org.teamkorea.backend.dto;

public class SignupResponseDto {

    private Long userId;
    private String username;
    private String name;
    private String email;
    private String message;

    public SignupResponseDto() {
    }

    public SignupResponseDto(Long userId, String username, String name, String email, String message) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.email = email;
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

    public String getMessage() {
        return message;
    }
}