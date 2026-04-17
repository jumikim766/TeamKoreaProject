package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.RefreshToken;
import org.teamkorea.backend.domain.User;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByUser(User user);

    void deleteAllByUser(User user);
}