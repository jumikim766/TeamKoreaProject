package org.teamkorea.backend.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.RefreshToken;
import org.teamkorea.backend.domain.User;
import org.teamkorea.backend.dto.LoginResponseDto;
import org.teamkorea.backend.dto.LoginUserDto;
import org.teamkorea.backend.dto.ReissueRequestDto;
import org.teamkorea.backend.dto.ReissueResponseDto;
import org.teamkorea.backend.dto.SignupRequestDto;
import org.teamkorea.backend.dto.SignupResponseDto;
import org.teamkorea.backend.repository.RefreshTokenRepository;
import org.teamkorea.backend.repository.UserRepository;
import org.teamkorea.backend.security.JwtUtil;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public SignupResponseDto signup(SignupRequestDto requestDto) {
        // 회원가입 요청값 필수 검증
        validateSignupRequest(requestDto);

        // 아이디/이메일 중복 검증
        validateDuplicate(requestDto);

        User user = User.builder()
                .username(requestDto.getUsername())
                .email(requestDto.getEmail())
                .passwordHash(passwordEncoder.encode(requestDto.getPassword()))
                .name(requestDto.getName())
                // TODO: 현재는 임시 byte[] 변환입니다. 추후 encryptionUtil.encrypt(requestDto.getPhone())로 교체 권장
                .phoneEnc(requestDto.getPhone().getBytes(StandardCharsets.UTF_8))
                // .gender(requestDto.getGender())
                // .age(requestDto.getAge())
                .role("USER")
                .status("ACTIVE")
                .provider("LOCAL")
                .build();

        User savedUser = userRepository.save(user);

        return new SignupResponseDto(
                savedUser.getUserId(),
                savedUser.getUsername(),
                savedUser.getName(),
                savedUser.getEmail()
        );
    }

    public LoginResponseDto login(String email, String password) {
        // 로그인 요청값 필수 검증
        validateLoginRequest(email, password);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!"LOCAL".equals(user.getProvider())) {
            throw new IllegalArgumentException("소셜 로그인 계정입니다. 일반 로그인을 사용할 수 없습니다.");
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        String refreshTokenHash = jwtUtil.hashToken(refreshToken);

        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                jwtUtil.getRefreshTokenExpiryInstant(),
                ZoneId.systemDefault()
        );

        // 한 계정 = 한 기기 로그인 정책
        // 새 로그인 시 기존 Refresh Token을 모두 삭제하여 이전 기기 로그인을 무효화
        refreshTokenRepository.deleteAllByUser(user);

        RefreshToken savedRefreshToken = new RefreshToken(user, refreshTokenHash, expiresAt);
        refreshTokenRepository.save(savedRefreshToken);

        // 로그인 성공 시 마지막 로그인 시간 갱신
        user.updateLastLoginAt();

        return new LoginResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                new LoginUserDto(
                        user.getUserId(),
                        user.getEmail(),
                        user.getName(),
                        user.getRole()
                )
        );
    }

    @Transactional(readOnly = true)
    public ReissueResponseDto reissue(ReissueRequestDto requestDto) {
        if (requestDto == null || requestDto.getRefreshToken() == null || requestDto.getRefreshToken().isBlank()) {
            throw new IllegalArgumentException("refreshToken이 누락되었습니다.");
        }

        String refreshToken = requestDto.getRefreshToken();

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 refreshToken입니다.");
        }

        String refreshTokenHash = jwtUtil.hashToken(refreshToken);

        RefreshToken savedToken = refreshTokenRepository.findByTokenHash(refreshTokenHash)
                .orElseThrow(() -> new IllegalArgumentException("저장된 토큰 정보를 찾을 수 없습니다."));

        // DB 기준 만료 시간까지 한 번 더 확인
        // 만료된 토큰이면 DB에서 삭제 후 재발급 차단
        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.deleteByTokenHash(refreshTokenHash);
            throw new IllegalArgumentException("유효하지 않거나 만료된 refreshToken입니다.");
        }

        String newAccessToken = jwtUtil.generateAccessToken(savedToken.getUser());

        return new ReissueResponseDto(
                newAccessToken,
                "Bearer"
        );
    }

    // 현재 기기 로그아웃
    // 전달받은 Refresh Token만 DB에서 삭제하여 해당 토큰 재사용을 차단
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("로그아웃 요청 정보가 올바르지 않습니다.");
        }

        String tokenHash = jwtUtil.hashToken(refreshToken);

        refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("인증 정보가 유효하지 않습니다."));

        refreshTokenRepository.deleteByTokenHash(tokenHash);
    }

    // 전체 로그아웃
    // 해당 사용자의 모든 Refresh Token을 삭제
    public void logoutAll(User user) {
        if (user == null) {
            throw new IllegalArgumentException("사용자 정보가 올바르지 않습니다.");
        }

        refreshTokenRepository.deleteAllByUser(user);
    }

    // 만료된 Refresh Token 정리
    // 스케줄러에서 주기적으로 호출하면 DB에 만료 토큰이 쌓이는 것을 방지
    public void deleteExpiredRefreshTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    private void validateSignupRequest(SignupRequestDto requestDto) {
        // 요청 객체 자체가 없는 경우 방지
        if (requestDto == null) {
            throw new IllegalArgumentException("회원가입 요청 정보가 올바르지 않습니다.");
        }

        // 일반 회원가입은 아이디 필수
        if (requestDto.getUsername() == null || requestDto.getUsername().isBlank()) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }

        // 일반 회원가입은 이메일 필수
        if (requestDto.getEmail() == null || requestDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        // 일반 회원가입은 비밀번호 필수
        // DB password_hash는 소셜 로그인 때문에 NULL 가능하지만, LOCAL 회원가입에서는 반드시 입력받아야 함
        if (requestDto.getPassword() == null || requestDto.getPassword().isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }

        // 일반 회원가입은 이름 필수
        if (requestDto.getName() == null || requestDto.getName().isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }

        // 현재 코드에서 phoneEnc 변환 시 getPhone().getBytes()를 사용하므로 phone도 필수 검증
        if (requestDto.getPhone() == null || requestDto.getPhone().isBlank()) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }
    }

    private void validateLoginRequest(String email, String password) {
        // 로그인은 이메일 필수
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        // 로그인은 비밀번호 필수
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
    }

    private void validateDuplicate(SignupRequestDto requestDto) {
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }
}