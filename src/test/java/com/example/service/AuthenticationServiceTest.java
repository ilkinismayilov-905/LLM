package com.example.service;

import com.example.dto.mapper.UserMapper;
import com.example.dto.request.LoginRequest;
import com.example.dto.request.RefreshTokenRequest;
import com.example.dto.request.RegisterRequest;
import com.example.dto.request.ResetPasswordRequest;
import com.example.dto.response.*;
import com.example.entity.PasswordResetToken;
import com.example.entity.RefreshToken;
import com.example.entity.User;
import com.example.enums.Role;
import com.example.exception.*;
import com.example.repository.UserRepository;
import com.example.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserMapper mapper;
    @Mock
    private EmailService emailService;
    @Mock
    private PasswordResetTokenService passwordResetTokenService;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;
    private UserResponse userResponse;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded_password")
                .firstName("John")
                .lastName("Doe")
                .role(Role.STUDENT)
                .isActive(true)
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role("STUDENT")
                .build();

        authentication = mock(Authentication.class);
    }

    @Test
    void shouldLoginSuccessfully() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("access_token");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(refreshTokenService.createOrUpdateRefreshToken(user)).thenReturn("refresh_token");
        when(mapper.toUserResponse(user)).thenReturn(userResponse);

        // Act
        LoginResponse response = authenticationService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("access_token", response.accessToken());
        assertEquals("refresh_token", response.refreshToken());
        assertEquals(userResponse, response.user());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldThrowExceptionWhenLoginUserNotFoundInDb() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("access_token");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> authenticationService.login(request));
    }

    @Test
    void shouldRegisterSuccessfully() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("new@example.com")
                .password("password123")
                .firstName("Jane")
                .lastName("Smith")
                .role("STUDENT")
                .build();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded_new");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateTokenFromEmail(user.getEmail())).thenReturn("access_token");
        when(refreshTokenService.createOrUpdateRefreshToken(user)).thenReturn("refresh_token");
        when(mapper.toUserResponse(user)).thenReturn(userResponse);

        // Act
        LoginResponse response = authenticationService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("access_token", response.accessToken());
        assertEquals("refresh_token", response.refreshToken());
        verify(emailService).sendRegisterConfirmationEmail(user.getEmail(), user.getFirstName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenRegisteringExistingEmail() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .build();
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateUserException.class, () -> authenticationService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("valid_refresh_token");
        RefreshToken dbToken = RefreshToken.builder().user(user).token("valid_refresh_token").build();

        when(refreshTokenService.validateRefreshToken(request.refreshToken())).thenReturn(Optional.of(dbToken));
        when(jwtTokenProvider.generateTokenFromEmail(user.getEmail())).thenReturn("new_access_token");
        when(refreshTokenService.createOrUpdateRefreshToken(user)).thenReturn("new_refresh_token");

        // Act
        RefreshTokenResponse response = authenticationService.refreshToken(request);

        // Assert
        assertNotNull(response);
        assertEquals("new_access_token", response.accessToken());
        assertEquals("new_refresh_token", response.refreshToken());
    }

    @Test
    void shouldThrowExceptionWhenRefreshingWithInvalidToken() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("invalid_token");
        when(refreshTokenService.validateRefreshToken(request.refreshToken())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> authenticationService.refreshToken(request));
    }

    @Test
    void shouldThrowExceptionWhenRefreshingForInactiveUser() {
        // Arrange
        user.setIsActive(false);
        RefreshTokenRequest request = new RefreshTokenRequest("valid_token");
        RefreshToken dbToken = RefreshToken.builder().user(user).token("valid_token").build();

        when(refreshTokenService.validateRefreshToken(request.refreshToken())).thenReturn(Optional.of(dbToken));

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> authenticationService.refreshToken(request));
    }

    @Test
    void shouldInitiatePasswordChangeSuccessfully() {
        // Arrange
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenService.generateResetToken(user)).thenReturn("reset-token-123");

        // Act
        PasswordResetTokenResponse response = authenticationService.initiatePasswordChange(authentication);

        // Assert
        assertNotNull(response);
        verify(emailService).sendPasswordResetEmail(eq(user.getEmail()), eq("reset-token-123"), anyString());
    }

    @Test
    void shouldThrowExceptionWhenInitiatingPasswordChangeForInactiveUser() {
        // Arrange
        user.setIsActive(false);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(InvalidPasswordException.class, () -> authenticationService.initiatePasswordChange(authentication));
    }

    @Test
    void shouldVerifyPasswordResetTokenSuccessfully() {
        // Arrange
        PasswordResetToken resetToken = PasswordResetToken.builder().user(user).token("valid-token").build();
        when(passwordResetTokenService.validateAndGetToken("valid-token")).thenReturn(resetToken);

        // Act
        TokenVerificationResponse response = authenticationService.verifyPasswordResetToken("valid-token");

        // Assert
        assertNotNull(response);
        assertEquals("Token is valid", response.message());
    }

    @Test
    void shouldResetPasswordSuccessfully() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "newPass123", "newPass123");
        PasswordResetToken resetToken = PasswordResetToken.builder().user(user).token("valid-token").build();

        when(passwordResetTokenService.validateAndGetToken(request.token())).thenReturn(resetToken);
        when(passwordEncoder.encode(request.newPassword())).thenReturn("encoded_new_pass");

        // Act
        ChangePasswordResponse response = authenticationService.resetPassword(request);

        // Assert
        assertNotNull(response);
        assertEquals("encoded_new_pass", user.getPassword());
        verify(userRepository).save(user);
        verify(passwordResetTokenService).markTokenAsUsed(resetToken);
        verify(emailService).sendPasswordChangeConfirmationEmail(user.getEmail(), user.getFirstName());
    }

    @Test
    void shouldThrowExceptionWhenResetPasswordsDoNotMatch() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "newPass123", "differentPass");
        PasswordResetToken resetToken = PasswordResetToken.builder().user(user).token("valid-token").build();

        when(passwordResetTokenService.validateAndGetToken(request.token())).thenReturn(resetToken);

        // Act & Assert
        assertThrows(InvalidPasswordException.class, () -> authenticationService.resetPassword(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenResetPasswordIsTooShort() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "12345", "12345");
        PasswordResetToken resetToken = PasswordResetToken.builder().user(user).token("valid-token").build();

        when(passwordResetTokenService.validateAndGetToken(request.token())).thenReturn(resetToken);

        // Act & Assert
        assertThrows(InvalidPasswordException.class, () -> authenticationService.resetPassword(request));
        verify(userRepository, never()).save(any(User.class));
    }
}