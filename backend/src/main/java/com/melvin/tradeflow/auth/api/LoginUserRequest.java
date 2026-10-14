package com.melvin.tradeflow.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NonNull;

public record LoginUserRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password
) {

    public LoginUserRequest {
        if (email != null) {
            email = email.strip();
        }
    }

    @Override
    @NonNull
    public String toString() {
        return "LoginUserRequest[email=%s, password=[REDACTED]]"
                .formatted(email);
    }
}
