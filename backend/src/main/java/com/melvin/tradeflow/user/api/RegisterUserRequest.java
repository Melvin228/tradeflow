package com.melvin.tradeflow.user.api;

import com.melvin.tradeflow.user.domain.UserConstraints;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NonNull;

import java.util.Locale;

public record RegisterUserRequest(
        @NotBlank
        @Email
        @Size(max = UserConstraints.MAX_EMAIL_LENGTH)
        String email,

        @NotBlank
        @Size(max = UserConstraints.MAX_DISPLAY_NAME_LENGTH)
        String displayName,

        @NotBlank
        @Size(min = UserConstraints.MIN_PASSWORD_LENGTH, max = UserConstraints.MAX_PASSWORD_LENGTH)
        String password
) {
    public RegisterUserRequest {
        if (email != null) {
            email = email.strip().toLowerCase(Locale.ROOT);
        }
        if (displayName != null) {
            displayName = displayName.strip();
        }
    }

    @Override
    @NonNull
    public String toString() {
        return "RegisterUserRequest[email=%s, displayName=%s, password=[REDACTED]]"
                .formatted(email, displayName);
    }
}
