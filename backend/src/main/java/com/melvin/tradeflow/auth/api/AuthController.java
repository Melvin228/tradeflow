package com.melvin.tradeflow.auth.api;

import com.melvin.tradeflow.auth.application.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AuthApiRoutes.BASE)
public class AuthController {
    private final AuthService authService;

    public AuthController(final AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(AuthApiRoutes.LOGIN)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginUserRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
