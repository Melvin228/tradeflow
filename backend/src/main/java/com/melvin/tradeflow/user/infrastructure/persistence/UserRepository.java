package com.melvin.tradeflow.user.infrastructure.persistence;

import com.melvin.tradeflow.user.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByEmail(String normalizedEmail);

    Optional<UserAuthenticationData> findProjectedByEmail(String email);

    interface UserAuthenticationData {
        UUID getId();
        String getEmail();
        String getPasswordHash();
        UserStatus getStatus();
    }
}
