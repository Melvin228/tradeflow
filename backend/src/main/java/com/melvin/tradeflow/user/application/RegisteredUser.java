package com.melvin.tradeflow.user.application;

import com.melvin.tradeflow.user.domain.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record RegisteredUser(
        UUID id,
        String email,
        String displayName,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
