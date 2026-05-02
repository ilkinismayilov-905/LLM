package com.example.service;

import com.example.entity.PasswordResetToken;
import com.example.entity.User;
import com.example.exception.InvalidPasswordTokenException;
import com.example.repository.PasswordResetTokenRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @InjectMocks
    private PasswordResetTokenService passwordResetTokenService;

    @Captor
    private ArgumentCaptor<PasswordResetToken> tokenCaptor;

    private User user;
    private PasswordResetToken validToken;

    @BeforeEach
    void setUp() {
        // @Value ilə oxunan field-i test mühitində manual set edirik
        ReflectionTestUtils.setField(passwordResetTokenService, "tokenExpirationMinutes", 60);

        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .isActive(true)
                .build();

        validToken = PasswordResetToken.builder()
                .id(1L)
                .token("valid-uuid-token")
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .isUsed(false)
                .build();
    }

    @Test
    void shouldGenerateResetTokenSuccessfully() {
        // Arrange
        PasswordResetToken oldToken = PasswordResetToken.builder()
                .id(2L)
                .token("old-uuid-token")
                .user(user)
                .isUsed(false)
                .build();

        // Köhnə, istifadə edilməmiş tokenləri tapdığını mock edirik
        when(tokenRepository.findByUserAndIsUsedFalse(user)).thenReturn(List.of(oldToken));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        String generatedToken = passwordResetTokenService.generateResetToken(user);

        // Assert
        assertNotNull(generatedToken);
        assertFalse(generatedToken.isEmpty());

        // Köhnə tokenin silindiyini yoxlayırıq
        verify(tokenRepository, times(1)).delete(oldToken);

        // Yeni tokenin düzgün parametrlərlə yadda saxlandığını yoxlayırıq
        verify(tokenRepository, times(1)).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();

        assertEquals(user, savedToken.getUser());
        assertEquals(generatedToken, savedToken.getToken());
        assertFalse(savedToken.getIsUsed());
        assertTrue(savedToken.getExpiryDate().isAfter(LocalDateTime.now()));
    }

    @Test
    void shouldValidateAndGetTokenSuccessfully() {
        // Arrange
        // (Fərz edirik ki, validToken.isValid() metodu daxildə isUsed=false və expiryDate>now() yoxlayır)
        // Mockito.spy istifadə edə bilərik, amma real obyektin metodu true qaytaracaq.
        PasswordResetToken spyToken = spy(validToken);
        when(spyToken.isValid()).thenReturn(true);
        when(tokenRepository.findByToken("valid-uuid-token")).thenReturn(Optional.of(spyToken));

        // Act
        PasswordResetToken result = passwordResetTokenService.validateAndGetToken("valid-uuid-token");

        // Assert
        assertNotNull(result);
        assertEquals("valid-uuid-token", result.getToken());
        verify(tokenRepository, times(1)).findByToken("valid-uuid-token");
    }

    @Test
    void shouldThrowExceptionWhenTokenNotFound() {
        // Arrange
        when(tokenRepository.findByToken("non-existent-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidPasswordTokenException.class,
                () -> passwordResetTokenService.validateAndGetToken("non-existent-token"));

        verify(tokenRepository, times(1)).findByToken("non-existent-token");
    }

    @Test
    void shouldThrowExceptionWhenTokenIsInvalidOrExpired() {
        // Arrange
        PasswordResetToken expiredOrUsedToken = spy(validToken);
        when(expiredOrUsedToken.isValid()).thenReturn(false); // Token ya vaxtı keçib, ya da istifadə olunub

        when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.of(expiredOrUsedToken));

        // Act & Assert
        InvalidPasswordTokenException exception = assertThrows(InvalidPasswordTokenException.class,
                () -> passwordResetTokenService.validateAndGetToken("invalid-token"));

        assertEquals("Token is invalid or expired", exception.getMessage());
        verify(tokenRepository, times(1)).findByToken("invalid-token");
    }

    @Test
    void shouldMarkTokenAsUsedSuccessfully() {
        // Arrange
        assertFalse(validToken.getIsUsed()); // Başlanğıcda false olmalıdır
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        passwordResetTokenService.markTokenAsUsed(validToken);

        // Assert
        assertTrue(validToken.getIsUsed()); // Statusun true-ya dəyişdiyini yoxlayırıq
        verify(tokenRepository, times(1)).save(validToken);
    }

    @Test
    void shouldCleanupExpiredTokensSuccessfully() {
        // Arrange
        doNothing().when(tokenRepository).deleteExpiredTokens(any(LocalDateTime.class));

        // Act
        passwordResetTokenService.cleanupExpiredTokens();

        // Assert
        verify(tokenRepository, times(1)).deleteExpiredTokens(any(LocalDateTime.class));
    }
}