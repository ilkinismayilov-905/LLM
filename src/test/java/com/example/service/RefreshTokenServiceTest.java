package com.example.service;

import com.example.entity.RefreshToken;
import com.example.entity.User;
import com.example.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Captor
    private ArgumentCaptor<RefreshToken> refreshTokenCaptor;

    private User user;
    private RefreshToken validToken;
    private RefreshToken expiredToken;

    @BeforeEach
    void setUp() {
        // @Value ilə gələn dəyəri (milisaniyə) test üçün təyin edirik
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationMs", 604800000L); // 7 gün

        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .isActive(true)
                .build();

        validToken = spy(RefreshToken.builder()
                .id(1L)
                .token("valid-refresh-token")
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(5)) // Gələcək vaxt
                .build());

        expiredToken = spy(RefreshToken.builder()
                .id(2L)
                .token("expired-refresh-token")
                .user(user)
                .expiryDate(LocalDateTime.now().minusDays(1)) // Keçmiş vaxt
                .build());
    }

    @Test
    void shouldCreateNewRefreshTokenWhenNoExistingToken() {
        // Arrange
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        String newToken = refreshTokenService.createOrUpdateRefreshToken(user);

        // Assert
        assertNotNull(newToken);
        assertFalse(newToken.isEmpty());

        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
        verify(refreshTokenRepository, times(1)).save(refreshTokenCaptor.capture());

        RefreshToken savedToken = refreshTokenCaptor.getValue();
        assertEquals(user, savedToken.getUser());
        assertEquals(newToken, savedToken.getToken());
        assertTrue(savedToken.getExpiryDate().isAfter(LocalDateTime.now()));
    }

    @Test
    void shouldUpdateRefreshTokenByDeletingOldAndCreatingNew() {
        // Arrange
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(validToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        String newToken = refreshTokenService.createOrUpdateRefreshToken(user);

        // Assert
        assertNotNull(newToken);

        // Köhnə tokenin silindiyini yoxlayırıq
        verify(refreshTokenRepository, times(1)).delete(validToken);

        // Yeni tokenin saxlanıldığını yoxlayırıq
        verify(refreshTokenRepository, times(1)).save(refreshTokenCaptor.capture());

        RefreshToken savedToken = refreshTokenCaptor.getValue();
        assertEquals(user, savedToken.getUser());
        assertNotEquals(validToken.getToken(), savedToken.getToken()); // Token UUID kimi fərqli olmalıdır
    }

    @Test
    void shouldReturnValidTokenWhenValidateRefreshToken() {
        // Arrange
        when(validToken.isValid()).thenReturn(true);
        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(validToken));

        // Act
        Optional<RefreshToken> result = refreshTokenService.validateRefreshToken("valid-refresh-token");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validToken, result.get());
        verify(refreshTokenRepository, times(1)).findByToken("valid-refresh-token");
    }

    @Test
    void shouldReturnEmptyWhenTokenIsExpiredDuringValidation() {
        // Arrange
        when(expiredToken.isValid()).thenReturn(false);
        when(refreshTokenRepository.findByToken("expired-refresh-token")).thenReturn(Optional.of(expiredToken));

        // Act
        Optional<RefreshToken> result = refreshTokenService.validateRefreshToken("expired-refresh-token");

        // Assert
        assertFalse(result.isPresent()); // filter tərəfindən silinməlidir
        verify(refreshTokenRepository, times(1)).findByToken("expired-refresh-token");
    }

    @Test
    void shouldReturnEmptyWhenTokenNotFoundDuringValidation() {
        // Arrange
        when(refreshTokenRepository.findByToken("non-existent")).thenReturn(Optional.empty());

        // Act
        Optional<RefreshToken> result = refreshTokenService.validateRefreshToken("non-existent");

        // Assert
        assertFalse(result.isPresent());
        verify(refreshTokenRepository, times(1)).findByToken("non-existent");
    }

    @Test
    void shouldGetValidRefreshTokenForUser() {
        // Arrange
        when(validToken.isValid()).thenReturn(true);
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(validToken));

        // Act
        Optional<RefreshToken> result = refreshTokenService.getRefreshToken(user);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validToken, result.get());
        verify(refreshTokenRepository, times(1)).findByUser(user);
    }

    @Test
    void shouldReturnEmptyWhenGetExpiredRefreshTokenForUser() {
        // Arrange
        when(expiredToken.isValid()).thenReturn(false);
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(expiredToken));

        // Act
        Optional<RefreshToken> result = refreshTokenService.getRefreshToken(user);

        // Assert
        assertFalse(result.isPresent());
        verify(refreshTokenRepository, times(1)).findByUser(user);
    }

    @Test
    void shouldDeleteRefreshTokenSuccessfully() {
        // Arrange
        doNothing().when(refreshTokenRepository).deleteByUser(user);

        // Act
        refreshTokenService.deleteRefreshToken(user);

        // Assert
        verify(refreshTokenRepository, times(1)).deleteByUser(user);
    }

    @Test
    void shouldCleanupExpiredTokensSuccessfully() {
        // Arrange
        doNothing().when(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));

        // Act
        refreshTokenService.cleanupExpiredTokens();

        // Assert
        verify(refreshTokenRepository, times(1)).deleteExpiredTokens(any(LocalDateTime.class));
    }
}