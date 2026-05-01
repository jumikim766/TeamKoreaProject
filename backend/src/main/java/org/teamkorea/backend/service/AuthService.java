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
        validateDuplicate(requestDto);

        User user = User.builder()
        .username(requestDto.getUsername())
        .email(requestDto.getEmail())
        .passwordHash(passwordEncoder.encode(requestDto.getPassword()))
        .name(requestDto.getName())
        // 임시 처리: 실제 암호화가 아니라 byte[] 변환만 수행
        .phoneEnc(requestDto.getPhone().getBytes(StandardCharsets.UTF_8))
        // .gender(requestDto.getGender())
        // .age(requestDto.getAge())
        .role("USER")
        .status("ACTIVE")
        .provider("LOCAL")
        .build();
       
        // 나중에 아래 코드로 수정
        // user.setPhoneEnc(encryptionUtil.encrypt(requestDto.getPhone()));

        User savedUser = userRepository.save(user);

        return new SignupResponseDto(
                savedUser.getUserId(),
                savedUser.getUsername(),
                savedUser.getName(),
                savedUser.getEmail()
        );
    }

    public LoginResponseDto login(String email, String password) {
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

        refreshTokenRepository.deleteAllByUser(user);

        RefreshToken savedRefreshToken = new RefreshToken(user, refreshTokenHash, expiresAt);
        refreshTokenRepository.save(savedRefreshToken);

        user.updateLastLoginAt(); // 마지막 로그인 시간 업데이트
       
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

    //Access Token 재발급
    @Transactional(readOnly = true)
    public ReissueResponseDto reissue(ReissueRequestDto requestDto) {

        //refreshToken null/blank 체크
        if (requestDto == null || requestDto.getRefreshToken() == null || requestDto.getRefreshToken().isBlank()) {
            throw new IllegalArgumentException("refreshToken이 누락되었습니다.");
        }

        String refreshToken = requestDto.getRefreshToken();

        //refreshToken 자체 유효성 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 refreshToken입니다.");
        }

        //DB 조회용 hash 생성
        String refreshTokenHash = jwtUtil.hashToken(refreshToken);

        //DB에 저장된 refresh token 조회
        RefreshToken savedToken = refreshTokenRepository.findByTokenHash(refreshTokenHash)
                .orElseThrow(() -> new IllegalArgumentException("저장된 토큰 정보를 찾을 수 없습니다."));

        //DB expires_at 재확인
        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 refreshToken입니다.");
        }

        //새 access token 발급
        String newAccessToken = jwtUtil.generateAccessToken(savedToken.getUser());

        return new ReissueResponseDto(
                newAccessToken,
                "Bearer"
        );
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