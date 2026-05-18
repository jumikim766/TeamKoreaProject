package org.teamkorea.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.teamkorea.backend.domain.DomainReputation;
import java.util.Optional;

@Repository
public interface DomainReputationRepository extends JpaRepository<DomainReputation, Long> {
    Optional<DomainReputation> findByDomain(String domain);
    boolean existsByDomain(String domain);
}