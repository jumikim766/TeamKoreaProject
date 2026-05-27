package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 보호
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false, length = 20, unique = true)
    private String username;// 로그인 아이디 또는 내부 식별자

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;// 사용자 이메일

    @Column(name = "password_hash", length = 255)
    private String passwordHash;// BCrypt 해시 비밀번호

    @Column(name = "name", nullable = false, length = 30)
    private String name;// 사용자 이름

    @Column(name = "phone_enc", columnDefinition = "VARBINARY(512)")
    private byte[] phoneEnc;// 암호화된 전화번호

    @Column(name = "gender", length = 10)
    private String gender;// FEMALE / MALE / OTHER 등

    @Column(name = "age")
    private Integer age;// 나이

    @Builder.Default
    @Column(name = "role", nullable = false, length = 20)
    private String role = "USER";// 기본 권한

    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";// 기본 계정 상태

    @Builder.Default
    @Column(name = "provider", nullable = false, length = 20)
    private String provider = "LOCAL";// 기본 로그인 제공자

    @Column(name = "provider_id", length = 255)
    private String providerId;// 소셜 로그인 제공자 ID

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;// 마지막 로그인 시각

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;// 계정 생성 시각

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;// 탈퇴 처리 시각

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null)
            this.createdAt = LocalDateTime.now();// 저장 전 생성 시각 자동 입력
        if (this.role == null)
            this.role = "USER";
        if (this.status == null)
            this.status = "ACTIVE";
        if (this.provider == null)
            this.provider = "LOCAL";
    }

    /** 소셜 로그인 사용자 정보 설정/갱신 */
    public void updateOAuthInfo(String username, String email, String name,
            String provider, String providerId) {
        if (this.username == null)
            this.username = username;
        this.email = email;
        this.name = name;
        this.provider = provider;
        this.providerId = providerId;
        this.role = "USER";
        this.status = "ACTIVE";
        this.lastLoginAt = LocalDateTime.now();
    }

    /** 마지막 로그인 시간 갱신 */
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /** 프로필(전화번호/이름/성별/나이) 수정 */
    public void updateProfile(byte[] phoneEnc, String name, String gender, Integer age) {
        if (phoneEnc != null)
            this.phoneEnc = phoneEnc;
        if (name != null && !name.isBlank())
            this.name = name;
        if (gender != null && !gender.isBlank())
            this.gender = gender;
        if (age != null)
            this.age = age;
    }

    /** 아이디/이메일 수정 (LOCAL 계정 전용) */
    public void updateAccountInfo(String username, String email) {
        if (username != null && !username.isBlank())
            this.username = username;
        if (email != null && !email.isBlank())
            this.email = email;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    /** 비밀번호 변경 */
    public void changePassword(String encodedPassword) {
        this.passwordHash = encodedPassword;
    }

    /** 탈퇴 여부 확인 */
    public boolean isDeleted() {
        return "DELETED".equals(this.status);
    }

    /** soft delete */
    public void withdraw() {
        this.status = "DELETED";
        this.deletedAt = LocalDateTime.now();
    }

}
