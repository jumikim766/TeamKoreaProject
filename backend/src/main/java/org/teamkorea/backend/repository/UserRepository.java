package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);//로그인용

    Optional<User> findByUsername(String username);//회원가입/기타용(사용자 식별/아이디 개념)
    
    //소셜 로그인
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    //중복체크
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}