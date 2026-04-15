package org.teamkorea.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.User;
import org.teamkorea.backend.dto.LoginRequestDto;
import org.teamkorea.backend.dto.LoginResponseDto;
import org.teamkorea.backend.dto.SignupRequestDto;
import org.teamkorea.backend.dto.SignupResponseDto;
import org.teamkorea.backend.repository.UserRepository;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public SignupResponseDto signup(SignupRequestDto requestDto) {
        validateDuplicate(requestDto);

        User user = new User();
        user.setUsername(requestDto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(requestDto.getPassword()));
        user.setName(requestDto.getName());
        user.setEmail(requestDto.getEmail());

        user.setPhoneEnc(null);

        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setProvider("LOCAL");

        User savedUser = userRepository.save(user);

        return new SignupResponseDto(
            savedUser.getUserId(),
            savedUser.getUsername(),
            savedUser.getName(),
            savedUser.getEmail(),
            "회원가입이 완료되었습니다."
        );
    }

    public LoginResponseDto login(LoginRequestDto requestDto) {
        User user = userRepository.findByUsername(requestDto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!"LOCAL".equals(user.getProvider())) {
            throw new IllegalArgumentException("소셜 로그인 계정입니다. 일반 로그인을 사용할 수 없습니다.");
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(requestDto.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        return new LoginResponseDto(
                user.getUserId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
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