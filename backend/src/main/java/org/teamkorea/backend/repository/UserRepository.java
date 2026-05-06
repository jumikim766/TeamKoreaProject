package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email); // 로그인용

    Optional<User> findByUsername(String username); // 회원가입/기타용

    // 소셜 로그인
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    // 중복 체크
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    // ===== 추가: ACTIVE 상태 사용자만 조회 =====
    Optional<User> findByUserIdAndStatus(Long userId, String status);
}