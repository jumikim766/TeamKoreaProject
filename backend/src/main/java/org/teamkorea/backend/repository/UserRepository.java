package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // [수정] 이메일로 사용자 조회 (로그인 및 중복 체크용)
    Optional<User> findByEmail(String email);

    // [유지] 사용자명으로 조회
    Optional<User> findByUsername(String username);

    // [유지] 소셜 로그인 (구글, 네이버 등)
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    // [유지] 중복 체크 기능
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    // [유지] 상태값(ACTIVE 등) 기준 조회
    Optional<User> findByUserIdAndStatus(Long userId, String status);

    // 이름 + 이메일로 사용자 조회 (아이디 찾기용)
    Optional<User> findByNameAndEmail(String name, String email);

    // 아이디(username) + 이메일로 사용자 조회 (비밀번호 찾기용)
    Optional<User> findByUsernameAndEmail(String username, String email);
}