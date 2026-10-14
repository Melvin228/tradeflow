package com.melvin.tradeflow.user.infrastructure.persistence;

import com.melvin.tradeflow.common.infrastructure.persistence.AbstractAuditedVersionedEntity;
import com.melvin.tradeflow.user.domain.InvalidUserDataException;
import com.melvin.tradeflow.user.domain.UserConstraints;
import com.melvin.tradeflow.user.domain.UserNotActiveException;
import com.melvin.tradeflow.user.domain.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "app_user")
public class UserEntity extends AbstractAuditedVersionedEntity {
    @Column(name = "email", nullable = false, unique = true, length = UserConstraints.MAX_EMAIL_LENGTH)
    private String email;

    @Column(name = "display_name", nullable = false, length = UserConstraints.MAX_DISPLAY_NAME_LENGTH)
    private String displayName;

    @Column(name = "password_hash", nullable = false, length = UserConstraints.MAX_PASSWORD_HASH_LENGTH)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    protected UserEntity() {
    }

    private UserEntity(String email, String displayName, String passwordHash) {
        this.email = normalizeEmail(email);
        this.displayName = normalizeDisplayName(displayName);
        this.passwordHash = validatePasswordHash(passwordHash);
    }

    public static UserEntity register(String email, String displayName, String encodedPasswordHash) {
        return new UserEntity(email, displayName, encodedPasswordHash);
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void recordSuccessfulLogin(Instant loginTime) {
        Objects.requireNonNull(loginTime, "loginTime must not be null");
        if (status != UserStatus.ACTIVE) {
            throw new UserNotActiveException();
        }
        this.lastLoginAt = loginTime;
    }

    private static String normalizeEmail(String email) {
        Objects.requireNonNull(email, "email must not be null");
        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);
        if (normalizedEmail.isEmpty()) {
            throw new InvalidUserDataException("email must not be blank");
        }
        if (normalizedEmail.length() > UserConstraints.MAX_EMAIL_LENGTH) {
            throw new InvalidUserDataException(
                    "email must not exceed %d characters".formatted(UserConstraints.MAX_EMAIL_LENGTH)
            );
        }
        return normalizedEmail;
    }

    private static String normalizeDisplayName(String displayName) {
        Objects.requireNonNull(displayName, "displayName must not be null");
        String normalizedDisplayName = displayName.strip();
        if (normalizedDisplayName.isEmpty()) {
            throw new InvalidUserDataException("displayName must not be blank");
        }
        if (normalizedDisplayName.length() > UserConstraints.MAX_DISPLAY_NAME_LENGTH) {
            throw new InvalidUserDataException(
                    "displayName must not exceed %d characters".formatted(UserConstraints.MAX_DISPLAY_NAME_LENGTH)
            );
        }
        return normalizedDisplayName;
    }

    private static String validatePasswordHash(String passwordHash) {
        Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        if (passwordHash.isBlank()) {
            throw new InvalidUserDataException("passwordHash must not be blank");
        }
        if (passwordHash.length() > UserConstraints.MAX_PASSWORD_HASH_LENGTH) {
            throw new InvalidUserDataException(
                    "passwordHash must not exceed %d characters".formatted(UserConstraints.MAX_PASSWORD_HASH_LENGTH)
            );
        }
        return passwordHash;
    }
}
