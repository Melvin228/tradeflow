package com.melvin.tradeflow.auth.application;

import com.melvin.tradeflow.user.domain.UserStatus;
import com.melvin.tradeflow.user.infrastructure.persistence.UserRepository;
import com.melvin.tradeflow.user.infrastructure.persistence.UserRepository.UserAuthenticationData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {
    private static final String EMAIL = "person@example.com";
    private static final String PASSWORD_HASH = "{pbkdf2}encoded-password-hash";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_normalizesEmailAndReturnsPrincipal() {
        UUID userId = UUID.randomUUID();
        UserAuthenticationData user = mock(UserAuthenticationData.class);
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn(EMAIL);
        when(user.getPasswordHash()).thenReturn(PASSWORD_HASH);
        when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(userRepository.findProjectedByEmail(EMAIL)).thenReturn(Optional.of(user));

        UserPrincipal principal = assertInstanceOf(
                UserPrincipal.class,
                userDetailsService.loadUserByUsername(" PERSON@EXAMPLE.COM ")
        );

        assertEquals(userId, principal.getUserId());
        assertEquals(EMAIL, principal.getUsername());
        assertEquals(PASSWORD_HASH, principal.getPassword());
        assertTrue(principal.isEnabled());
        assertTrue(principal.isAccountNonLocked());
        assertTrue(principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        verify(userRepository).findProjectedByEmail(EMAIL);
    }

    @Test
    void loadUserByUsername_whenUserDoesNotExist_throwsUsernameNotFoundException() {
        when(userRepository.findProjectedByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(EMAIL)
        );
    }

    @Test
    void loadUserByUsername_whenUsernameIsBlank_throwsWithoutQueryingRepository() {
        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("  ")
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void principalMapsLockedAndDisabledStatusesToSecurityFlags() {
        UserPrincipal lockedPrincipal = new UserPrincipal(
                UUID.randomUUID(), EMAIL, PASSWORD_HASH, UserStatus.LOCKED
        );
        UserPrincipal disabledPrincipal = new UserPrincipal(
                UUID.randomUUID(), EMAIL, PASSWORD_HASH, UserStatus.DISABLED
        );

        assertFalse(lockedPrincipal.isAccountNonLocked());
        assertFalse(lockedPrincipal.isEnabled());
        assertTrue(disabledPrincipal.isAccountNonLocked());
        assertFalse(disabledPrincipal.isEnabled());
    }
}
