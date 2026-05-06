package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.RefreshToken;
import org.teamkorea.backend.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Refresh Token 검증 / 재발급 시 사용
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // 특정 사용자의 Refresh Token 목록 조회
    List<RefreshToken> findAllByUser(User user);

    // 현재 기기 로그아웃: 전달받은 Refresh Token만 삭제
    void deleteByTokenHash(String tokenHash);

    // 전체 기기 로그아웃: 해당 사용자의 모든 Refresh Token 삭제
    void deleteAllByUser(User user);

    // 만료된 Refresh Token 정리
    void deleteByExpiresAtBefore(LocalDateTime now);
}