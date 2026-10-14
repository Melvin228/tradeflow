package com.melvin.tradeflow.user.application;

import com.melvin.tradeflow.user.domain.UserStatus;
import com.melvin.tradeflow.user.infrastructure.persistence.UserEntity;
import com.melvin.tradeflow.user.infrastructure.persistence.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserEntityRegistrationServiceTest {
    private static final String RAW_PASSWORD = "long-test-password";
    private static final String ENCODED_PASSWORD = "{pbkdf2}encoded-password-hash";
    private static final String NORMALIZED_EMAIL = "person@example.com";
    private static final String DISPLAY_NAME = "Test User";
    private static final Instant TEST_TIMESTAMP = Instant.parse("2026-10-12T00:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    @Test
    void register_normalizesEmail_encodesPassword_andReturnsSafeUserData() {
        when(userRepository.existsByEmail(NORMALIZED_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity userEntity = invocation.getArgument(0);
            ReflectionTestUtils.setField(userEntity, "id", java.util.UUID.randomUUID());
            ReflectionTestUtils.setField(userEntity, "createdAt", TEST_TIMESTAMP);
            ReflectionTestUtils.setField(userEntity, "updatedAt", TEST_TIMESTAMP);
            assertEquals(ENCODED_PASSWORD, ReflectionTestUtils.getField(userEntity, "passwordHash"));
            return userEntity;
        });

        RegisteredUser registeredUser = userRegistrationService.register(
                new RegisterUserCommand(" PERSON@EXAMPLE.COM ", DISPLAY_NAME, RAW_PASSWORD)
        );

        assertEquals(NORMALIZED_EMAIL, registeredUser.email());
        assertEquals(DISPLAY_NAME, registeredUser.displayName());
        assertEquals(UserStatus.ACTIVE, registeredUser.status());
        assertEquals(TEST_TIMESTAMP, registeredUser.createdAt());
        verify(passwordEncoder).encode(RAW_PASSWORD);
    }

    @Test
    void register_whenEmailAlreadyExists_rejectsWithoutEncodingPassword() {
        when(userRepository.existsByEmail(NORMALIZED_EMAIL)).thenReturn(true);

        assertThrows(
                UserEmailAlreadyRegisteredException.class,
                () -> userRegistrationService.register(
                        new RegisterUserCommand("PERSON@example.com", DISPLAY_NAME, RAW_PASSWORD)
                )
        );

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void register_whenConcurrentInsertViolatesEmailConstraint_returnsDuplicateEmailError() {
        when(userRepository.existsByEmail(NORMALIZED_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        ConstraintViolationException constraintViolation = new ConstraintViolationException(
                "duplicate email", null, "uk_app_user_email"
        );
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenThrow(
                new DataIntegrityViolationException("duplicate email", constraintViolation)
        );

        assertThrows(
                UserEmailAlreadyRegisteredException.class,
                () -> userRegistrationService.register(
                        new RegisterUserCommand(NORMALIZED_EMAIL, DISPLAY_NAME, RAW_PASSWORD)
                )
        );
    }
}
