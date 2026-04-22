package com.example.controller;

import com.example.dto.request.LoginRequest;
import com.example.dto.request.RegisterRequest;
import com.example.dto.response.LoginResponse;
import com.example.dto.response.UserResponse;
import com.example.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;


    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        loginRequest = new LoginRequest(
                "user@school.com",
                "Password123!"
        );

        registerRequest = new RegisterRequest(
                "newuser@school.com",
                "SecurePassword123!",
                "John",
                "Doe",
                "TEACHER"
        );

        UserResponse userResponse = new UserResponse(
                1L,
                "user@school.com",
                "John",
                "Doe",
                "TEACHER",
                true
        );

        loginResponse = new LoginResponse(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "Bearer",
                userResponse
        );
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // Arrange
        when(authenticationService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("user@school.com"));
    }

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        // Arrange
        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.user.email").exists());
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsBlank() throws Exception {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest(
                "",
                "Password123!"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenPasswordIsBlank() throws Exception {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest(
                "user@school.com",
                ""
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest(
                "invalid-email",
                "Password123!"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenRegisteringWithMissingFirstName() throws Exception {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest(
                "newuser@school.com",
                "SecurePassword123!",
                "",
                "Doe",
                "TEACHER"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenRegisteringWithMissingRole() throws Exception {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest(
                "newuser@school.com",
                "SecurePassword123!",
                "John",
                "Doe",
                ""
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}



