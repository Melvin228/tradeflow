package com.melvin.tradeflow.user.application;

import com.melvin.tradeflow.user.infrastructure.persistence.UserEntity;
import com.melvin.tradeflow.user.infrastructure.persistence.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserRegistrationService {
    private static final String EMAIL_UNIQUE_CONSTRAINT = "uk_app_user_email";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisteredUser register(RegisterUserCommand command) {
        String normalizedEmail = command.email().strip().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserEmailAlreadyRegisteredException();
        }

        final String encodedPassword = passwordEncoder.encode(command.rawPassword());
        final UserEntity userEntity = UserEntity.register(normalizedEmail, command.displayName(), encodedPassword);

        try {
            final UserEntity savedUserEntity = userRepository.saveAndFlush(userEntity);
            return new RegisteredUser(
                    savedUserEntity.getId(),
                    savedUserEntity.getEmail(),
                    savedUserEntity.getDisplayName(),
                    savedUserEntity.getStatus(),
                    savedUserEntity.getCreatedAt(),
                    savedUserEntity.getUpdatedAt()
            );
        } catch (DataIntegrityViolationException exception) {
            if (violatesEmailUniqueConstraint(exception)) {
                throw new UserEmailAlreadyRegisteredException();
            }
            throw exception;
        }
    }

    private boolean violatesEmailUniqueConstraint(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation
                    && EMAIL_UNIQUE_CONSTRAINT.equals(violation.getConstraintName())) {
                return true;
            }
        }
        return false;
    }
}
