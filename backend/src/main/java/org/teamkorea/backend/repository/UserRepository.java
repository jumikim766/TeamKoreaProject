package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    boolean existsByUsername(String username); 

    boolean existsByEmail(String email);
}