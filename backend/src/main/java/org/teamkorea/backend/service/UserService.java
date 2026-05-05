package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.User;
import org.teamkorea.backend.dto.UserMeResponseDto;
import org.teamkorea.backend.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserMeResponseDto getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

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
        .build();
    }
}