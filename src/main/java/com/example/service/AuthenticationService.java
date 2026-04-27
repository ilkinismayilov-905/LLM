package com.example.service;

import com.example.dto.mapper.UserMapper;
import com.example.dto.request.LoginRequest;
import com.example.dto.request.RefreshTokenRequest;
import com.example.dto.request.RegisterRequest;
import com.example.dto.request.ResetPasswordRequest;
import com.example.dto.response.LoginResponse;
import com.example.dto.response.RefreshTokenResponse;
import com.example.dto.response.UserResponse;
import com.example.dto.response.ChangePasswordResponse;
import com.example.dto.response.PasswordResetTokenResponse;
import com.example.dto.response.TokenVerificationResponse;
import com.example.entity.RefreshToken;
import com.example.entity.User;
import com.example.entity.PasswordResetToken;
import com.example.enums.Role;
import com.example.exception.*;
import com.example.repository.UserRepository;
import com.example.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper mapper;
    private final EmailService emailService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final RefreshTokenService refreshTokenService;

    public LoginResponse login(LoginRequest request) {
        log.info("Attempting login for email: {}", request.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        String accessToken = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Store or update refresh token in database
        String refreshToken = refreshTokenService.createOrUpdateRefreshToken(user);

        UserResponse userResponse = mapper.toUserResponse(user);

        log.info("User logged in successfully: {}", request.email());
        return LoginResponse.of(accessToken, refreshToken, userResponse);
    }

    public LoginResponse register(RegisterRequest request) {
        log.info("Attempting registration for email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("User already exists with email: " + request.email());
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(Role.valueOf(request.role().toUpperCase()))
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", request.email());

        String accessToken = jwtTokenProvider.generateTokenFromEmail(savedUser.getEmail());
        // Store refresh token in database
        String refreshToken = refreshTokenService.createOrUpdateRefreshToken(savedUser);
        UserResponse userResponse = mapper.toUserResponse(savedUser);

        return LoginResponse.of(accessToken, refreshToken, userResponse);
    }

    /**
     * Refresh token ilə yeni access token + yeni refresh token yarat (token rotation).
     * Refresh token'ı database'den validate et.
     *
     * @throws InvalidTokenException refresh token etibarsız və ya tip yanlışdırsa
     */
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String token = request.refreshToken();
        log.info("Refresh token request received");

        // 1. Validate refresh token from database
        RefreshToken dbToken = refreshTokenService.validateRefreshToken(token)
                .orElseThrow(() -> {
                    log.warn("Invalid or expired refresh token from database");
                    return new InvalidTokenException("Invalid or expired refresh token");
                });

        User user = dbToken.getUser();

        // 2. Verify user is active
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("Inactive user tried to refresh token: {}", user.getEmail());
            throw new InvalidTokenException("User account is inactive");
        }

        // 3. Generate new access token
        String newAccessToken = jwtTokenProvider.generateTokenFromEmail(user.getEmail());

        // 4. Create new refresh token (delete old, create new)
        String newRefreshToken = refreshTokenService.createOrUpdateRefreshToken(user);

        log.info("Token refreshed successfully for email: {}", user.getEmail());
        return RefreshTokenResponse.of(newAccessToken, newRefreshToken);
    }

    /**
     * Step 1: Initiate password change - send verification code to email
     */
    public PasswordResetTokenResponse initiatePasswordChange(Authentication authentication) {
        String email = authentication.getName();
        log.info("Password change initiated for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("Inactive user tried to reset password: {}", email);
            throw new InvalidPasswordException("User account is inactive");
        }

        // Generate password reset token
        String resetToken = passwordResetTokenService.generateResetToken(user);
        log.info("Password reset token generated for user: {}", email);

        // Build reset link (frontend URL)
        String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;

        // Send email with verification code and link
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken, resetLink);

        log.info("Password reset email sent to: {}", user.getEmail());
        return PasswordResetTokenResponse.success("Verification code sent to your email");
    }

    /**
     * Step 2: Verify if the token is valid
     */
    public TokenVerificationResponse verifyPasswordResetToken(String token) {
        log.info("Verifying password reset token");

        PasswordResetToken resetToken = passwordResetTokenService.validateAndGetToken(token);

        log.info("Password reset token is valid for user: {}", resetToken.getUser().getEmail());

        return TokenVerificationResponse.valid("Token is valid");

    }

    /**
     * Step 3: Reset password with verified token
     */
    public ChangePasswordResponse resetPassword(ResetPasswordRequest request) {
        log.info("Password reset requested with token");

        // Validate token
        PasswordResetToken resetToken = passwordResetTokenService.validateAndGetToken(request.token());

        // Validate password match
        if (!request.newPassword().equals(request.confirmPassword())) {
            log.warn("Passwords do not match for user: {}", resetToken.getUser().getEmail());
            throw new InvalidPasswordException("Passwords do not match");
        }

        // Validate password length
        if (request.newPassword().length() < 6) {
            log.warn("Password is too short for user: {}", resetToken.getUser().getEmail());
            throw new InvalidPasswordException("Password must be at least 6 characters long");
        }

        // Update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password updated for user: {}", user.getEmail());

        // Mark token as used
        passwordResetTokenService.markTokenAsUsed(resetToken);

        // Send confirmation email
        emailService.sendPasswordChangeConfirmationEmail(user.getEmail(), user.getFirstName());

        log.info("Password reset successfully for user: {}", user.getEmail());
        return ChangePasswordResponse.success("Password changed successfully");
    }
}
