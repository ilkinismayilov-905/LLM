package com.example.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS  = "ACCESS";
    private static final String REFRESH = "REFRESH";

    // ── Access token config ──────────────────────────────────────────────────
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:900000}")
    private long jwtExpirationMs;

    // ── Refresh token config ─────────────────────────────────────────────────
    @Value("${app.jwt.refresh-secret}")
    private String jwtRefreshSecret;

    @Value("${app.jwt.refresh-expiration:604800000}")
    private long jwtRefreshExpirationMs;

    // ── Token generation ─────────────────────────────────────────────────────

    /** Authentication obyektindən ACCESS token yarat */
    public String generateToken(Authentication authentication) {
        log.info("Generating access token for user: {}", authentication.getName());
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return buildAccessToken(userPrincipal.getEmail());
    }

    /** Email-dən ACCESS token yarat (register üçün) */
    public String generateTokenFromEmail(String email) {
        log.info("Generating access token for email: {}", email);
        return buildAccessToken(email);
    }

    /** Email-dən REFRESH token yarat — ayrıca refresh signing key ilə */
    public String generateRefreshToken(String email) {
        log.info("Generating refresh token for email: {}", email);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtRefreshExpirationMs);

        return Jwts.builder()
                .subject(email)
                .claim(TOKEN_TYPE_CLAIM, REFRESH)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getRefreshSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getAccessSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException ex) {
            log.error("Invalid access token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("Access token claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getRefreshSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return REFRESH.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (JwtException ex) {
            log.error("Invalid refresh token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("Refresh token claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token, getAccessSigningKey()).getSubject();
    }
    public String getEmailFromRefreshToken(String token) {
        return parseClaims(token, getRefreshSigningKey()).getSubject();
    }


    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseClaims(token, getRefreshSigningKey());
            return REFRESH.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (Exception ex) {
            return false;
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private String buildAccessToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(email)
                .claim(TOKEN_TYPE_CLAIM, ACCESS)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getAccessSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parseClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getAccessSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getRefreshSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtRefreshSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
