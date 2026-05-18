package org.teamkorea.backend.service;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.RefreshToken;
import org.teamkorea.backend.domain.User;
import org.teamkorea.backend.dto.LoginResponseDto;
import org.teamkorea.backend.dto.LoginUserDto;
import org.teamkorea.backend.dto.ReissueResponseDto;
import org.teamkorea.backend.dto.SignupRequestDto;
import org.teamkorea.backend.dto.SignupResponseDto;
import org.teamkorea.backend.exception.BusinessException;
import org.teamkorea.backend.exception.ErrorCode;
import org.teamkorea.backend.repository.RefreshTokenRepository;
import org.teamkorea.backend.repository.UserRepository;
import org.teamkorea.backend.security.CryptoUtil;
import org.teamkorea.backend.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor // 수동 생성자 → Lombok으로 통일
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CryptoUtil cryptoUtil;

    public SignupResponseDto signup(SignupRequestDto requestDto) {
        validateDuplicate(requestDto);

        byte[] phoneEnc = null;
        if (requestDto.getPhone() != null && !requestDto.getPhone().isBlank()) {
            phoneEnc = cryptoUtil.encrypt(requestDto.getPhone());
        }

        User user = User.builder()
                .username(requestDto.getUsername())
                .email(requestDto.getEmail())
                .passwordHash(passwordEncoder.encode(requestDto.getPassword()))
                .name(requestDto.getName())
                .phoneEnc(phoneEnc)
                .gender(requestDto.getGender())
                .age(requestDto.getAge())
                .role("USER")
                .status("ACTIVE")
                .provider("LOCAL")
                .build();

        User savedUser = userRepository.save(user);

        return SignupResponseDto.builder()
                .userId(savedUser.getUserId())
                .username(savedUser.getUsername())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .status(savedUser.getStatus())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    public LoginResponseDto login(String email, String password) {

        log.info("로그인 요청");

        validateLoginRequest(email, password);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!"LOCAL".equals(user.getProvider())) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT, "소셜 로그인 계정입니다. 일반 로그인을 사용할 수 없습니다.");
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {

            log.warn("로그인 실패");

            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        String refreshTokenHash = jwtUtil.hashToken(refreshToken);

        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                jwtUtil.getRefreshTokenExpiryInstant(), ZoneId.systemDefault());

        // 한 계정 = 한 기기 정책: 기존 토큰 전부 삭제
        refreshTokenRepository.deleteAllByUser(user);
        refreshTokenRepository.save(new RefreshToken(user, refreshTokenHash, expiresAt));

        user.updateLastLoginAt();

        log.info("로그인 성공");

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(LoginUserDto.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .role(user.getRole())
                        .build())
                .build();
    }

    // readOnly 제거: 만료 토큰 삭제(쓰기) 가능성이 있으므로
    @Transactional
    public ReissueResponseDto reissue(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "refreshToken 쿠키가 없습니다.");
        }
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않거나 만료된 refreshToken입니다.");
        }
        String refreshTokenHash = jwtUtil.hashToken(refreshToken);
        RefreshToken savedToken = refreshTokenRepository.findByTokenHash(refreshTokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "저장된 토큰 정보를 찾을 수 없습니다."));

        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.deleteByTokenHash(refreshTokenHash);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않거나 만료된 refreshToken입니다.");
        }

        User user = savedToken.getUser();

        // 기존 refreshToken 삭제
        refreshTokenRepository.deleteByTokenHash(refreshTokenHash);

        // 새 accessToken + 새 refreshToken 발급
        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);
        String newRefreshTokenHash = jwtUtil.hashToken(newRefreshToken);

        LocalDateTime newExpiresAt = LocalDateTime.ofInstant(
                jwtUtil.getRefreshTokenExpiryInstant(),
                ZoneId.systemDefault());

        // 새 refreshToken 저장
        refreshTokenRepository.save(new RefreshToken(user, newRefreshTokenHash, newExpiresAt));

        return ReissueResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .build();
    }

    /** logout(String refreshToken)은 null 허용으로 살짝 완화 */
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank())
            return; // 이미 만료/없음 → 멱등 처리
        String tokenHash = jwtUtil.hashToken(refreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(rt -> refreshTokenRepository.deleteByTokenHash(tokenHash));
    }

    /** 전체 기기 로그아웃 */
    public void logoutAll(User user) {
        if (user == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT, "사용자 정보가 올바르지 않습니다.");
        }
        refreshTokenRepository.deleteAllByUser(user);
    }

    /** 스케줄러에서 호출: 만료된 Refresh Token 정리 */
    public void deleteExpiredRefreshTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    // ===== private 검증 메서드 =====
    private void validateLoginRequest(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이메일은 필수입니다.");
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "비밀번호는 필수입니다.");
        }
    }

    private void validateDuplicate(SignupRequestDto requestDto) {
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
    }
}
