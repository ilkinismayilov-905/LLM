package com.example.service;

import com.example.dto.mapper.EntityToDtoMapper;
import com.example.dto.request.LoginRequest;
import com.example.dto.request.RefreshTokenRequest;
import com.example.dto.request.RegisterRequest;
import com.example.dto.response.LoginResponse;
import com.example.dto.response.RefreshTokenResponse;
import com.example.dto.response.UserResponse;
import com.example.entity.User;
import com.example.enums.Role;
import com.example.exception.DuplicateUserException;
import com.example.exception.InvalidTokenException;
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
    private final EntityToDtoMapper mapper;

    public LoginResponse login(LoginRequest request) {
        log.info("Attempting login for email: {}", request.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        String accessToken  = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

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

        String accessToken  = jwtTokenProvider.generateTokenFromEmail(savedUser.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getEmail());
        UserResponse userResponse = mapper.toUserResponse(savedUser);

        return LoginResponse.of(accessToken, refreshToken, userResponse);
    }

    /**
     * Refresh token ilə yeni access token + yeni refresh token yarat (token rotation).
     *
     * @throws InvalidTokenException refresh token etibarsız və ya tip yanlışdırsa
     */
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String token = request.refreshToken();
        log.info("Refresh token request received");

        // 1. İmza + expiration + tokenType yoxlaması
        if (!jwtTokenProvider.validateRefreshToken(token)) {
            log.warn("Invalid or expired refresh token");
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        // 2. Email-i refresh key ilə parse et
        String email = jwtTokenProvider.getEmailFromRefreshToken(token);

        // 3. İstifadəçinin hələ aktiv olduğunu yoxla
        userRepository.findByEmail(email)
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                .orElseThrow(() -> new InvalidTokenException("User not found or inactive"));

        // 4. Yeni token cütü yarat (refresh token rotation)
        String newAccessToken  = jwtTokenProvider.generateTokenFromEmail(email);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

        log.info("Token refreshed successfully for email: {}", email);
        return RefreshTokenResponse.of(newAccessToken, newRefreshToken);
    }
}
