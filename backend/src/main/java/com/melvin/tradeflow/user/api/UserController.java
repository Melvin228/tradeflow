package com.melvin.tradeflow.user.api;

import com.melvin.tradeflow.user.application.RegisterUserCommand;
import com.melvin.tradeflow.user.application.RegisteredUser;
import com.melvin.tradeflow.user.application.UserRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserApiRoutes.BASE)
public class UserController {
    private final UserRegistrationService userRegistrationService;

    public UserController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @PostMapping
    @Operation(summary = "Register a new user", description = "Register user with provided email, displayName, and password")
    @ApiResponse(responseCode = "201", description = "User successfully created")
    @ApiResponse(responseCode = "409", description = "Email already registered")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        RegisteredUser registeredUser = userRegistrationService.register(
                new RegisterUserCommand(request.email(), request.displayName(), request.password())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(registeredUser));
    }
}
