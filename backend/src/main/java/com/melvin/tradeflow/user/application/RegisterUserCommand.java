package com.melvin.tradeflow.user.application;

import org.jspecify.annotations.NonNull;

public record RegisterUserCommand(String email, String displayName, String rawPassword) {
    @Override
    @NonNull
    public String toString() {
        return "RegisterUserCommand[email=%s, displayName=%s, rawPassword=[REDACTED]]"
                .formatted(email, displayName);
    }
}
