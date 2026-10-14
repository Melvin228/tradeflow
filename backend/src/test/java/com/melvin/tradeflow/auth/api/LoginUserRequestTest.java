package com.melvin.tradeflow.auth.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LoginUserRequestTest {
    private static final String EMAIL = " USER@EXAMPLE.COM ";
    private static final String PASSWORD = " password ";

    @Test
    void constructor_normalizeEmail_butPreservePassword() {
        LoginUserRequest request = new LoginUserRequest(EMAIL, PASSWORD);
        assertEquals("USER@EXAMPLE.COM", request.email());
        assertEquals(PASSWORD, request.password());
    }

    @Test
    void toString_doesNotIncludePassword() {
        LoginUserRequest request = new LoginUserRequest(" sometestingemail@testing.com", PASSWORD);
        assertFalse(request.toString().contains(PASSWORD));
    }
}
