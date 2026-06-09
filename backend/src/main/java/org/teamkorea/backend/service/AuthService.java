package org.teamkorea.backend.service;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.EmailVerificationCode;
import org.teamkorea.backend.domain.RefreshToken;
import org.teamkorea.backend.domain.User;
import org.teamkorea.backend.dto.FindUsernameResponseDto;
import org.teamkorea.backend.dto.FindUsernameSendCodeRequestDto;
import org.teamkorea.backend.dto.LoginResponseDto;
import org.teamkorea.backend.dto.LoginUserDto;
import org.teamkorea.backend.dto.PasswordResetRequestDto;
import org.teamkorea.backend.dto.PasswordResetSendCodeRequestDto;
import org.teamkorea.backend.dto.ReissueResponseDto;
import org.teamkorea.backend.dto.SignupRequestDto;
import org.teamkorea.backend.dto.SignupResponseDto;
import org.teamkorea.backend.dto.VerifyCodeRequestDto;
import org.teamkorea.backend.dto.SignupSendCodeRequestDto;
import org.teamkorea.backend.exception.BusinessException;
import org.teamkorea.backend.exception.ErrorCode;
import org.teamkorea.backend.repository.EmailVerificationCodeRepository;
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
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final EmailVerificationService emailVerificationService;

    // ===== 회원가입 인증번호 발송 =====
    public void sendSignupCode(SignupSendCodeRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL, "이미 사용 중인 이메일입니다.");
        }

        // 최근 발송한 회원가입 인증번호가 아직 만료되지 않았으면 재발송 차단
        emailVerificationCodeRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(request.getEmail(), "SIGNUP")
                .ifPresent(latestCode -> {
                    if (!latestCode.isExpired()) {
                        throw new BusinessException(
                                ErrorCode.INVALID_INPUT,
                                "이미 발송된 인증번호가 있습니다. 3분 후 다시 요청해주세요.");
                    }
                });

        String code = emailVerificationService.createVerificationCode();

        EmailVerificationCode verificationCode = EmailVerificationCode.builder()
                .email(request.getEmail())
                .code(code)
                .purpose("SIGNUP")
                .verified(false)
                .expiresAt(LocalDateTime.now().plusMinutes(3))// 인증번호는 3분 동안 유효
                .build();

        emailVerificationCodeRepository.save(verificationCode);

        emailVerificationService.sendVerificationCode(request.getEmail(), code);
    }

    public SignupResponseDto signup(SignupRequestDto requestDto) {
        validateDuplicate(requestDto);

        EmailVerificationCode verificationCode = emailVerificationCodeRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(
                        requestDto.getEmail(),
                        "SIGNUP")
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_INPUT,
                        "이메일 인증이 필요합니다."));

        if (!verificationCode.isVerified()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "이메일 인증이 필요합니다.");
        }

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

        emailVerificationCodeRepository.deleteAllByEmailAndPurpose(
                requestDto.getEmail(),
                "SIGNUP");

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

    // ===== 회원가입 인증번호 검증 =====
    @Transactional
    public void verifySignupCode(VerifyCodeRequestDto request) {
        emailVerificationService.verifyCode(
                request.getEmail(),
                request.getCode(),
                "SIGNUP");
    }

    public LoginResponseDto login(String username, String password) {

        log.info("로그인 요청");

        validateLoginRequest(username, password);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!"LOCAL".equals(user.getProvider())) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT, "소셜 로그인 계정입니다. 일반 로그인을 사용할 수 없습니다.");
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {

            log.warn("로그인 실패");

            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
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

    // ===== 아이디 찾기 인증번호 발송 =====
    public void sendFindUsernameCode(FindUsernameSendCodeRequestDto request) {
        User user = userRepository.findByNameAndEmail(request.getName(), request.getEmail())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND));

        String code = emailVerificationService.createVerificationCode();

        EmailVerificationCode verificationCode = EmailVerificationCode.builder()
                .email(user.getEmail())
                .code(code)
                .purpose("FIND_USERNAME")
                .verified(false)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        emailVerificationCodeRepository.save(verificationCode);

        emailVerificationService.sendVerificationCode(user.getEmail(), code);
    }

    // ===== 아이디 찾기 인증번호 확인 =====
    @Transactional
    public FindUsernameResponseDto verifyFindUsernameCode(VerifyCodeRequestDto request) {

        emailVerificationService.verifyCode(
                request.getEmail(),
                request.getCode(),
                "FIND_USERNAME");

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND));

        return new FindUsernameResponseDto(maskUsername(user.getUsername()));
    }

    // ===== 비밀번호 찾기 인증번호 발송 =====
    public void sendPasswordResetCode(PasswordResetSendCodeRequestDto request) {
        User user = userRepository.findByUsernameAndEmail(request.getUsername(), request.getEmail())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND));

        String code = emailVerificationService.createVerificationCode();

        EmailVerificationCode verificationCode = EmailVerificationCode.builder()
                .email(user.getEmail())
                .code(code)
                .purpose("PASSWORD_RESET")
                .verified(false)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        emailVerificationCodeRepository.save(verificationCode);

        emailVerificationService.sendVerificationCode(user.getEmail(), code);
    }

    // ===== 비밀번호 재설정 =====
    @Transactional
    public void resetPassword(PasswordResetRequestDto request) {

        emailVerificationService.verifyCode(
                request.getEmail(),
                request.getCode(),
                "PASSWORD_RESET");

        User user = userRepository.findByUsernameAndEmail(
                request.getUsername(),
                request.getEmail()).orElseThrow(
                        () -> new BusinessException(
                                ErrorCode.USER_NOT_FOUND));

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());

        user.changePassword(encodedPassword);
    }

    // ===== private 검증 메서드 =====
    private void validateLoginRequest(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "아이디는 필수입니다.");
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

    // ===== username 마스킹 메서드 =====
    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) {
            return username;
        }

        int visibleLength = Math.min(3, username.length());
        String visible = username.substring(0, visibleLength);
        String masked = "*".repeat(username.length() - visibleLength);

        return visible + masked;
    }
}