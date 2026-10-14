package com.melvin.tradeflow.user.infrastructure.persistence;

import com.melvin.tradeflow.user.domain.UserConstraints;
import com.melvin.tradeflow.user.domain.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserEntityTest {
    private static final String EMAIL = "person@example.com";
    private static final String DISPLAY_NAME = "Test User";
    private static final String PASSWORD_HASH = "{encoded-password-hash}";

    @Test
    void register_normalizesEmailAndDisplayName_andStartsActive() {
        UserEntity userEntity = UserEntity.register(
                "  PERSON@EXAMPLE.COM ",
                "  " + DISPLAY_NAME + "  ",
                PASSWORD_HASH
        );

        assertEquals(EMAIL, userEntity.getEmail());
        assertEquals(DISPLAY_NAME, userEntity.getDisplayName());
        assertEquals(UserStatus.ACTIVE, userEntity.getStatus());
    }

    @Test
    void register_rejectsBlankOrOversizedValues() {
        assertThrows(IllegalArgumentException.class,
                () -> UserEntity.register("  ", DISPLAY_NAME, PASSWORD_HASH));
        assertThrows(IllegalArgumentException.class,
                () -> UserEntity.register(EMAIL, "  ", PASSWORD_HASH));
        assertThrows(IllegalArgumentException.class,
                () -> UserEntity.register(EMAIL, DISPLAY_NAME, " "));
        String oversizedEmail = "a".repeat(UserConstraints.MAX_EMAIL_LENGTH + 1);
        assertThrows(IllegalArgumentException.class,
                () -> UserEntity.register(oversizedEmail, DISPLAY_NAME, PASSWORD_HASH));
        String oversizedDisplayName = "a".repeat(UserConstraints.MAX_DISPLAY_NAME_LENGTH + 1);
        assertThrows(IllegalArgumentException.class,
                () -> UserEntity.register(EMAIL, oversizedDisplayName, PASSWORD_HASH));
        String oversizedPasswordHash = "x".repeat(UserConstraints.MAX_PASSWORD_HASH_LENGTH + 1);
        assertThrows(IllegalArgumentException.class,
                () -> UserEntity.register(EMAIL, DISPLAY_NAME, oversizedPasswordHash));
    }

    @Test
    void recordSuccessfulLogin_storesLoginInstant() {
        UserEntity userEntity = UserEntity.register(EMAIL, DISPLAY_NAME, PASSWORD_HASH);
        Instant loginTime = Instant.parse("2026-10-12T00:00:00Z");

        userEntity.recordSuccessfulLogin(loginTime);

        assertEquals(loginTime, userEntity.getLastLoginAt());
    }

    @Test
    void recordSuccessfulLogin_rejectsNullInstant() {
        UserEntity userEntity = UserEntity.register(EMAIL, DISPLAY_NAME, PASSWORD_HASH);

        assertThrows(NullPointerException.class, () -> userEntity.recordSuccessfulLogin(null));
    }

}
