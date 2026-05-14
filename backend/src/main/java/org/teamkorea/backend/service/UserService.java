package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.User;
import org.teamkorea.backend.dto.*;
import org.teamkorea.backend.exception.BusinessException;
import org.teamkorea.backend.exception.ErrorCode;
import org.teamkorea.backend.repository.UserRepository;
import org.teamkorea.backend.security.CryptoUtil;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CryptoUtil cryptoUtil;

    public UserMeResponseDto getMyInfo(Long userId) {
        User user = getActiveUser(userId);

        String phoneMasked = null;
        if (user.getPhoneEnc() != null) {
            phoneMasked = maskPhone(cryptoUtil.decrypt(user.getPhoneEnc()));
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

    @Transactional
    public UserUpdateResponseDto updateMyInfo(Long userId, UserUpdateRequestDto requestDto) {
        boolean allBlank =
                (requestDto.getUsername() == null || requestDto.getUsername().isBlank()) &&
                (requestDto.getEmail()    == null || requestDto.getEmail().isBlank())    &&
                (requestDto.getName()     == null || requestDto.getName().isBlank())     &&
                (requestDto.getPhone()    == null || requestDto.getPhone().isBlank())    &&
                (requestDto.getGender()   == null || requestDto.getGender().isBlank())   &&
                requestDto.getAge() == null;

        if (allBlank) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "수정할 정보가 없습니다.");
        }

        User user = getActiveUser(userId);

        boolean wantsAccountChange =
                (requestDto.getUsername() != null && !requestDto.getUsername().isBlank()) ||
                (requestDto.getEmail()    != null && !requestDto.getEmail().isBlank());

        if (wantsAccountChange) {
            if (!"LOCAL".equals(user.getProvider())) {
                throw new BusinessException(
                        ErrorCode.INVALID_INPUT,
                        "소셜 로그인 계정은 아이디 또는 이메일을 수정할 수 없습니다.");
            }

            if (requestDto.getUsername() != null
                    && !requestDto.getUsername().isBlank()
                    && !requestDto.getUsername().equals(user.getUsername())
                    && userRepository.existsByUsername(requestDto.getUsername())) {
                throw new BusinessException(ErrorCode.CONFLICT, "이미 사용 중인 아이디입니다.");
            }

            if (requestDto.getEmail() != null
                    && !requestDto.getEmail().isBlank()
                    && !requestDto.getEmail().equals(user.getEmail())
                    && userRepository.existsByEmail(requestDto.getEmail())) {
                throw new BusinessException(ErrorCode.CONFLICT, "이미 사용 중인 이메일입니다.");
            }

            user.updateAccountInfo(requestDto.getUsername(), requestDto.getEmail());
        }

        byte[] phoneEnc = user.getPhoneEnc();
        if (requestDto.getPhone() != null && !requestDto.getPhone().isBlank()) {
            phoneEnc = cryptoUtil.encrypt(requestDto.getPhone());
        }

        user.updateProfile(
                phoneEnc,
                requestDto.getName()   != null && !requestDto.getName().isBlank()
                        ? requestDto.getName()   : user.getName(),
                requestDto.getGender() != null && !requestDto.getGender().isBlank()
                        ? requestDto.getGender() : user.getGender(),
                requestDto.getAge()    != null
                        ? requestDto.getAge()    : user.getAge()
        );

        String phoneMasked = (phoneEnc != null)
                ? maskPhone(cryptoUtil.decrypt(phoneEnc)) : null;

        return UserUpdateResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .gender(user.getGender())
                .age(user.getAge())
                .phoneMasked(phoneMasked)
                .build();
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequestDto requestDto) {
        User user = getActiveUser(userId);

        if (!"LOCAL".equals(user.getProvider())) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT, "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
        }
        if (requestDto.getCurrentPassword() == null || requestDto.getCurrentPassword().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "현재 비밀번호를 입력해주세요.");
        }
        if (requestDto.getNewPassword() == null || requestDto.getNewPassword().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "새 비밀번호를 입력해주세요.");
        }
        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "현재 비밀번호가 올바르지 않습니다.");
        }
        if (passwordEncoder.matches(requestDto.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT, "새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        user.changePassword(passwordEncoder.encode(requestDto.getNewPassword()));
    }

    @Transactional
    public void deleteMyAccount(Long userId, UserDeleteRequestDto requestDto) {
        User user = getActiveUser(userId);

        if ("LOCAL".equals(user.getProvider())) {
            if (requestDto.getPassword() == null || requestDto.getPassword().isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "비밀번호 확인이 필요합니다.");
            }
            if (!passwordEncoder.matches(requestDto.getPassword(), user.getPasswordHash())) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "비밀번호가 올바르지 않습니다.");
            }
        }
        

        user.withdraw();
    }

    public DuplicateCheckResponseDto checkUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "아이디를 입력해주세요.");
        }
        return new DuplicateCheckResponseDto(!userRepository.existsByUsername(username));
    }

    public DuplicateCheckResponseDto checkEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이메일을 입력해주세요.");
        }
        return new DuplicateCheckResponseDto(!userRepository.existsByEmail(email));
    }

    // ===== private 헬퍼 =====

    private User getActiveUser(Long userId) {
        return userRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."));
    }

    /**
     * 전화번호 마스킹: 010XXXXXXXX → 010****XXXX
     * 하이픈 포함 형식(010-XXXX-XXXX)도 처리하도록 먼저 숫자만 추출 후 마스킹.
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) return null;

        String digits = phone.replaceAll("[^0-9]", "");

        if (digits.length() != 11) return phone; // 형식 미달 시 원문 반환

        return digits.substring(0, 3) + "****" + digits.substring(7);
    }
}
