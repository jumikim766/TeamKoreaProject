package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.teamkorea.backend.domain.DomainReputation;

import java.util.Optional;

public interface DomainReputationRepository extends JpaRepository<DomainReputation, Long> {

    Optional<DomainReputation> findByDomain(String domain);

    boolean existsByDomain(String domain);
}