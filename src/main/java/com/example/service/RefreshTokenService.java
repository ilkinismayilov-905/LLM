package com.example.service;

import com.example.entity.RefreshToken;
import com.example.entity.User;
import com.example.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration:604800000}")
    private long refreshTokenExpirationMs;

    /**
     * Create or update refresh token for user.
     * If token exists, delete old one and create new one.
     * If token doesn't exist, create new one.
     */
    public String createOrUpdateRefreshToken(User user) {
        log.info("Creating/updating refresh token for user: {}", user.getEmail());

        // Delete existing token if any
        refreshTokenRepository.findByUser(user).ifPresent(token -> {
            log.debug("Deleting old refresh token for user: {}", user.getEmail());
            refreshTokenRepository.delete(token);
        });

        // Create new token
        String tokenValue = java.util.UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusNanos(refreshTokenExpirationMs * 1_000_000L);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(expiryDate)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("New refresh token created for user: {}", user.getEmail());
        return tokenValue;
    }

    /**
     * Validate refresh token exists and not expired
     */
    public Optional<RefreshToken> validateRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(RefreshToken::isValid);
    }

    /**
     * Get refresh token for user
     */
    public Optional<RefreshToken> getRefreshToken(User user) {
        return refreshTokenRepository.findByUser(user)
                .filter(RefreshToken::isValid);
    }

    /**
     * Delete refresh token for user
     */
    public void deleteRefreshToken(User user) {
        log.info("Deleting refresh token for user: {}", user.getEmail());
        refreshTokenRepository.deleteByUser(user);
    }

    /**
     * Clean up expired tokens (called by scheduler)
     */
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteExpiredTokens(now);
        log.info("Expired refresh tokens cleaned up");
    }
}

