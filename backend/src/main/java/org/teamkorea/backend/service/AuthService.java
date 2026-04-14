package org.teamkorea.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.User;
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

        // phone_enc가 VARBINARY라면 지금은 일단 null로 두는 게 안전합니다.
        // 나중에 암호화 로직 붙일 때 처리하세요.
        // user.setPhoneEnc(...);

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

    private void validateDuplicate(SignupRequestDto requestDto) {
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }
}