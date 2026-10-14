package com.melvin.tradeflow.auth.application;

import com.melvin.tradeflow.auth.api.LoginResponse;
import com.melvin.tradeflow.auth.api.LoginUserRequest;
import com.melvin.tradeflow.user.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    private static final String EMAIL = "person@example.com";
    private static final String PASSWORD = "raw-password";
    private static final String TOKEN = "signed-jwt";
    private static final Instant NOW = Instant.parse("2026-10-14T00:00:00Z");

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtEncoder jwtEncoder;

    @Test
    void loginAuthenticatesCredentialsAndReturnsSignedTokenWithExpectedClaims() {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(
                userId, EMAIL, "{pbkdf2}password-hash", UserStatus.ACTIVE
        );
        Authentication authenticated = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities()
        );
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authenticated);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(
                Jwt.withTokenValue(TOKEN)
                        .header("alg", "HS256")
                        .subject(EMAIL)
                        .issuedAt(NOW)
                        .expiresAt(NOW.plusSeconds(900))
                        .build()
        );

        AuthService authService = new AuthService(
                authenticationManager,
                jwtEncoder,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofMinutes(15)
        );
        LoginResponse response = authService.login(new LoginUserRequest(EMAIL, PASSWORD));

        assertEquals(TOKEN, response.token());
        assertEquals(NOW, response.createdAt().toInstant());
        assertEquals(NOW.plusSeconds(900), response.expiresAt().toInstant());

        ArgumentCaptor<Authentication> authenticationCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(authenticationCaptor.capture());
        assertFalse(authenticationCaptor.getValue().isAuthenticated());
        assertEquals(EMAIL, authenticationCaptor.getValue().getName());
        assertEquals(PASSWORD, authenticationCaptor.getValue().getCredentials());

        ArgumentCaptor<JwtEncoderParameters> jwtParametersCaptor =
                ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(jwtParametersCaptor.capture());
        assertEquals(EMAIL, jwtParametersCaptor.getValue().getClaims().getSubject());
        assertEquals(userId.toString(), jwtParametersCaptor.getValue().getClaims().getClaim("uid"));
        assertEquals(
                principal.getAuthorities().stream().map(authority -> authority.getAuthority()).toList(),
                jwtParametersCaptor.getValue().getClaims().getClaimAsStringList("roles")
        );
    }
}
