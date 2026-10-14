package com.melvin.tradeflow.user.api;

import com.melvin.tradeflow.user.application.RegisteredUser;
import com.melvin.tradeflow.user.domain.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String displayName,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponse from(RegisteredUser user) {
        return new UserResponse(
                user.id(),
                user.email(),
                user.displayName(),
                user.status(),
                user.createdAt(),
                user.updatedAt()
        );
    }
}
