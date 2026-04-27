package com.example.controller;

import com.example.dto.request.LoginRequest;
import com.example.dto.request.RefreshTokenRequest;
import com.example.dto.request.RegisterRequest;
import com.example.dto.request.VerifyPasswordResetTokenRequest;
import com.example.dto.request.ResetPasswordRequest;
import com.example.dto.response.LoginResponse;
import com.example.dto.response.RefreshTokenResponse;
import com.example.dto.response.ChangePasswordResponse;
import com.example.dto.response.PasswordResetTokenResponse;
import com.example.dto.response.TokenVerificationResponse;
import com.example.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Refresh token ilə yeni access token + yeni refresh token al.
     * Bu endpoint yalnız refresh token qəbul edir — access token ilə çağırılarsa 400 xəta verir.
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Step 1: Initiate password change - send verification code to email
     * PUT /api/v1/auth/change-password/request
     */
    @PostMapping("/change-password/request")
    public ResponseEntity<PasswordResetTokenResponse> requestPasswordChange(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PasswordResetTokenResponse response = authenticationService.initiatePasswordChange(authentication);
        return ResponseEntity.ok(response);
    }

    /**
     * Step 2: Verify password reset token
     * GET /api/v1/auth/change-password/verify?token=xxx
     */
    @PostMapping("/change-password/verify")
    public ResponseEntity<TokenVerificationResponse> verifyPasswordResetToken(
            @Valid @RequestBody VerifyPasswordResetTokenRequest request) {
        TokenVerificationResponse response = authenticationService.verifyPasswordResetToken(request.token());
        return ResponseEntity.ok(response);
    }

    /**
     * Step 3: Complete password reset with verified token
     * POST /api/v1/auth/change-password/confirm
     */
    @PostMapping("/change-password/confirm")
    public ResponseEntity<ChangePasswordResponse> confirmPasswordChange(
            @Valid @RequestBody ResetPasswordRequest request) {
        ChangePasswordResponse response = authenticationService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
}
