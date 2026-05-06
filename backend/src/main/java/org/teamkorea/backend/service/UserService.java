package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.User;
import org.teamkorea.backend.dto.*;
import org.teamkorea.backend.repository.UserRepository;
import org.teamkorea.backend.security.CryptoUtil;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // ===== 추가: 비밀번호 암호화/검증용 =====
    private final PasswordEncoder passwordEncoder;
    private final CryptoUtil cryptoUtil;

    // ===== 기존 유지/수정: ACTIVE 사용자만 조회 =====
    public UserMeResponseDto getMyInfo(Long userId) {
        User user = getActiveUser(userId);

        String phoneMasked = null;

        // ===== 추가: 복호화 후 마스킹 =====
        if (user.getPhoneEnc() != null) {
            String phone = cryptoUtil.decrypt(user.getPhoneEnc());
            phoneMasked = maskPhone(phone);
        }

        return UserMeResponseDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .provider(user.getProvider())
                .gender(user.getGender())
                .age(user.getAge())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .phoneMasked(phoneMasked)
                .build();
    }

    // ===== 추가: 회원정보 수정 =====
    @Transactional
     public UserUpdateResponseDto updateMyInfo(Long userId, UserUpdateRequestDto requestDto) {
        User user = getActiveUser(userId);

        byte[] phoneEnc = null;

        // ===== 추가: phone이 들어온 경우에만 저장 =====
        if (requestDto.getPhone() != null && !requestDto.getPhone().isBlank()) {
            phoneEnc = cryptoUtil.encrypt(requestDto.getPhone());
        }

        // ===== 수정: 기존 phoneEnc 포함 updateProfile 사용 =====
        user.updateProfile(
                phoneEnc,
                requestDto.getName(),
                requestDto.getGender(),
                requestDto.getAge()
        );

        return UserUpdateResponseDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .gender(user.getGender())
                .age(user.getAge())
                // ===== 실제 값 기준으로 마스킹 =====
                .phoneMasked(maskPhone(requestDto.getPhone()))
                .build();
    }

    // ===== 추가: 비밀번호 변경 =====
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequestDto requestDto) {
        User user = getActiveUser(userId);

        if (!"LOCAL".equals(user.getProvider())) {
            throw new IllegalArgumentException("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
        }

        if (requestDto.getCurrentPassword() == null || requestDto.getCurrentPassword().isBlank()) {
            throw new IllegalArgumentException("현재 비밀번호를 입력해주세요.");
        }

        if (requestDto.getNewPassword() == null || requestDto.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("새 비밀번호를 입력해주세요.");
        }

        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        if (passwordEncoder.matches(requestDto.getNewPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        // ===== 추가: 새 비밀번호 암호화 후 저장 =====
        user.changePassword(passwordEncoder.encode(requestDto.getNewPassword()));
    }

    // ===== 추가: 회원탈퇴 soft delete =====
    @Transactional
    public void deleteMyAccount(Long userId, UserDeleteRequestDto requestDto) {
        User user = getActiveUser(userId);

        if ("LOCAL".equals(user.getProvider())) {
            if (requestDto.getPassword() == null || requestDto.getPassword().isBlank()) {
                throw new IllegalArgumentException("비밀번호 확인이 필요합니다.");
            }

            if (!passwordEncoder.matches(requestDto.getPassword(), user.getPasswordHash())) {
                throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
            }
        }

        user.withdraw();
    }

    // ===== 추가: username 중복 체크 =====
    public DuplicateCheckResponseDto checkUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }

        boolean exists = userRepository.existsByUsername(username);

        return new DuplicateCheckResponseDto(!exists);
    }

    // ===== 추가: email 중복 체크 =====
    public DuplicateCheckResponseDto checkEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        boolean exists = userRepository.existsByEmail(email);

        return new DuplicateCheckResponseDto(!exists);
    }

    // ===== 추가: ACTIVE 사용자 조회 공통 메서드 =====
    private User getActiveUser(Long userId) {
        return userRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

     // ===== 추가: 전화번호 마스킹 처리 =====
    private String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        if (phone.length() != 11) {
            return phone;
        }

        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}