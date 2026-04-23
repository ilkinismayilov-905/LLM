package com.example.service;

import com.example.entity.PasswordResetToken;
import com.example.entity.User;
import com.example.exception.InvalidPasswordTokenException;
import com.example.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository tokenRepository;

    @Value("${app.password-reset.expiration-minutes:60}")
    private int tokenExpirationMinutes;

    /**
     * Generate a new password reset token for the user.
     * If user already has a valid token, delete it first and create new one.
     */
    public String generateResetToken(User user) {
        log.info("Generating password reset token for user: {}", user.getEmail());

        // Delete all previous unused tokens for this user
        tokenRepository.findByUserAndIsUsedFalse(user).forEach(token -> {
            log.debug("Deleting old password reset token for user: {}", user.getEmail());
            tokenRepository.delete(token);
        });

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(tokenExpirationMinutes);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .isUsed(false)
                .build();

        tokenRepository.save(resetToken);
        log.info("Password reset token generated for user: {}", user.getEmail());
        return token;
    }

    /**
     * Validate and get the password reset token
     */
    public PasswordResetToken validateAndGetToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidPasswordTokenException("Invalid reset token"));

        if (!resetToken.isValid()) {
            log.warn("Invalid or expired token for user: {}", resetToken.getUser().getEmail());
            throw new InvalidPasswordTokenException("Token is invalid or expired");
        }

        return resetToken;
    }

    /**
     * Mark token as used after successful password reset
     */
    public void markTokenAsUsed(PasswordResetToken resetToken) {
        resetToken.setIsUsed(true);
        tokenRepository.save(resetToken);
        log.info("Password reset token marked as used for user: {}", resetToken.getUser().getEmail());
    }

    /**
     * Clean up expired tokens (called by scheduler)
     */
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.deleteExpiredTokens(now);
        log.info("Expired password reset tokens cleaned up");
    }
}

