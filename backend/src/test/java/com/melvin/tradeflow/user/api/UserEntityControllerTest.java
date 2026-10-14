package com.melvin.tradeflow.user.api;

import com.melvin.tradeflow.user.application.RegisteredUser;
import com.melvin.tradeflow.user.application.UserEmailAlreadyRegisteredException;
import com.melvin.tradeflow.user.application.UserRegistrationService;
import com.melvin.tradeflow.user.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserEntityControllerTest {
    private static final String REGISTER_PATH = UserApiRoutes.BASE;
    private static final UUID REGISTERED_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final String NORMALIZED_EMAIL = "person@example.com";
    private static final String DISPLAY_NAME = "Test User";
    private static final String RAW_PASSWORD = "long-test-password";
    private static final Instant CREATED_AT = Instant.parse("2026-10-12T00:00:00Z");
    private static final String VALID_REQUEST = """
            {
              "email": " PERSON@EXAMPLE.COM ",
              "displayName": "%s",
              "password": "%s"
            }
            """.formatted(DISPLAY_NAME, RAW_PASSWORD);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRegistrationService userRegistrationService;

    @Test
    void register_returnsCreatedSafeUserResponse() throws Exception {
        when(userRegistrationService.register(any())).thenReturn(new RegisteredUser(
                REGISTERED_USER_ID,
                NORMALIZED_EMAIL,
                DISPLAY_NAME,
                UserStatus.ACTIVE,
                CREATED_AT,
                CREATED_AT
        ));

        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(REGISTERED_USER_ID.toString()))
                .andExpect(jsonPath("$.email").value(NORMALIZED_EMAIL))
                .andExpect(jsonPath("$.displayName").value(DISPLAY_NAME))
                .andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(userRegistrationService).register(any());
    }

    @Test
    void register_withInvalidRequest_returnsBadRequestWithoutCallingService() throws Exception {
        String invalidRequest = """
                {
                  "email": "not-an-email",
                  "displayName": " ",
                  "password": "short"
                }
                """;

        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(userRegistrationService, never()).register(any());
    }

    @Test
    void register_whenEmailAlreadyExists_returnsConflict() throws Exception {
        when(userRegistrationService.register(any())).thenThrow(new UserEmailAlreadyRegisteredException());

        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("An account with this email already exists"));
    }
}
