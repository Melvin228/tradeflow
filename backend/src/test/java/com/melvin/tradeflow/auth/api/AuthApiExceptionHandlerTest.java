package com.melvin.tradeflow.auth.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthApiExceptionHandlerTest {
    @Test
    void authenticationFailureReturnsGenericUnauthorizedProblem() {
        AuthApiExceptionHandler handler = new AuthApiExceptionHandler();

        var problem = handler.handleAuthenticationFailure(
                new BadCredentialsException("specific internal message")
        );

        assertEquals(HttpStatus.UNAUTHORIZED.value(), problem.getStatus());
        assertEquals("Authentication failed", problem.getTitle());
        assertEquals("Email or password is incorrect.", problem.getDetail());
        assertEquals("AUTHENTICATION_FAILED", problem.getProperties().get("code"));
    }
}
