package com.melvin.tradeflow.auth.application;

import com.melvin.tradeflow.auth.api.LoginResponse;
import com.melvin.tradeflow.auth.api.LoginUserRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final Clock clock;
    private final Duration accessTokenTtl;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            Clock clock,
            @Value("${app.security.jwt.access-token-ttl:PT15M}") Duration accessTokenTtl
    ) {
        if (accessTokenTtl.isZero() || accessTokenTtl.isNegative()) {
            throw new IllegalArgumentException("JWT access-token TTL must be greater than zero");
        }
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
        this.accessTokenTtl = accessTokenTtl;
    }

    public LoginResponse login(LoginUserRequest loginUserRequest) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        loginUserRequest.email(),
                        loginUserRequest.password()
                )
        );
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        Instant createdAt = clock.instant();
        Instant expiresAt = createdAt.plus(accessTokenTtl);
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(principal.getUsername())
                .issuedAt(createdAt)
                .expiresAt(expiresAt)
                .claim("uid", principal.getUserId().toString())
                .claim("roles", authorities)
                .build();
        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        claims
                )
        ).getTokenValue();

        return new LoginResponse(token, Date.from(createdAt), Date.from(expiresAt));
    }
}
