package com.melvin.tradeflow.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;

import java.time.Instant;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtConfigurationTest {
    private final JwtConfiguration jwtConfiguration = new JwtConfiguration();

    @Test
    void encoderAndDecoderRoundTripSignedJwt() {
        String secret = Base64.getEncoder().encodeToString(new byte[32]);
        var key = jwtConfiguration.jwtSecretKey(secret);
        var encoder = jwtConfiguration.jwtEncoder(key);
        var decoder = jwtConfiguration.jwtDecoder(key);
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("person@example.com")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .claim("uid", "user-123")
                .claim("roles", List.of("ROLE_USER"))
                .build();

        String token = encoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
        ).getTokenValue();
        var decoded = decoder.decode(token);

        assertEquals("person@example.com", decoded.getSubject());
        assertEquals("user-123", decoded.getClaimAsString("uid"));
        assertEquals(List.of("ROLE_USER"), decoded.getClaimAsStringList("roles"));
    }

    @Test
    void secretMustDecodeToAtLeastThirtyTwoBytes() {
        String weakSecret = Base64.getEncoder().encodeToString(new byte[31]);

        assertThrows(IllegalStateException.class, () -> jwtConfiguration.jwtSecretKey(weakSecret));
    }
}
