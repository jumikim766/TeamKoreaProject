package org.teamkorea.backend.service;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.User;
import org.teamkorea.backend.dto.LoginResponseDto;
import org.teamkorea.backend.dto.SignupRequestDto;
import org.teamkorea.backend.dto.SignupResponseDto;
import org.teamkorea.backend.repository.UserRepository;
import org.teamkorea.backend.security.JwtUtil;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public SignupResponseDto signup(SignupRequestDto requestDto) {
        validateDuplicate(requestDto);

        User user = new User();
        user.setUsername(requestDto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(requestDto.getPassword()));
        user.setName(requestDto.getName());
        user.setEmail(requestDto.getEmail());

        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setProvider("LOCAL");

        // 임시 처리: 실제 암호화가 아니라 byte[] 변환만 수행
        user.setPhoneEnc(requestDto.getPhone().getBytes(StandardCharsets.UTF_8));
        //나중에 아래 코드로 수정
        //user.setPhoneEnc(encryptionUtil.encrypt(requestDto.getPhone()));

        User savedUser = userRepository.save(user);

        return new SignupResponseDto(
                savedUser.getUserId(),
                savedUser.getUsername(),
                savedUser.getName(),
                savedUser.getEmail(),
                "회원가입이 완료되었습니다."
        );
    }

    @Transactional(readOnly = true)
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

        return new LoginResponseDto(
                user.getUserId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                accessToken,
                "로그인에 성공했습니다."
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