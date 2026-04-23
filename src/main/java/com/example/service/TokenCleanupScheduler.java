package com.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenService passwordResetTokenService;

    /**
     * Clean up expired refresh tokens every 1 hour
     * Runs at: 00:00, 01:00, 02:00, etc.
     */
    @Scheduled(fixedDelay = 3600000, initialDelay = 300000)
    public void cleanupExpiredRefreshTokens() {
        log.info("Starting cleanup of expired refresh tokens");
        try {
            refreshTokenService.cleanupExpiredTokens();
            log.info("Refresh tokens cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error cleaning up expired refresh tokens: {}", e.getMessage(), e);
        }
    }

    /**
     * Clean up expired password reset tokens every 30 minutes
     */
    @Scheduled(fixedDelay = 1800000, initialDelay = 300000)
    public void cleanupExpiredPasswordTokens() {
        log.info("Starting cleanup of expired password reset tokens");
        try {
            passwordResetTokenService.cleanupExpiredTokens();
            log.info("Password reset tokens cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error cleaning up expired password reset tokens: {}", e.getMessage(), e);
        }
    }
}

