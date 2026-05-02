package com.example.service;

import com.example.exception.EmailSendingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    private final String targetEmail = "user@example.com";

    @Test
    void shouldSendPasswordResetEmailSuccessfully() {
        // Arrange
        String resetToken = "123456";
        String resetLink = "http://localhost:3000/reset?token=123456";

        // Act
        emailService.sendPasswordResetEmail(targetEmail, resetToken, resetLink);

        // Assert
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals("noreply@lms.com", sentMessage.getFrom());
        assertEquals(targetEmail, Objects.requireNonNull(sentMessage.getTo())[0]);
        assertEquals("Password Reset Request - UMS System", sentMessage.getSubject());
        assertTrue(Objects.requireNonNull(sentMessage.getText()).contains(resetToken));
        assertTrue(sentMessage.getText().contains(resetLink));
    }

    @Test
    void shouldThrowExceptionWhenPasswordResetEmailFails() {
        // Arrange
        doThrow(new MailSendException("SMTP connection failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        EmailSendingException exception = assertThrows(EmailSendingException.class,
                () -> emailService.sendPasswordResetEmail(targetEmail, "token", "link"));

        assertTrue(exception.getMessage().contains("Failed to send password reset email"));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldSendPasswordChangeConfirmationEmailSuccessfully() {
        // Arrange
        String firstName = "Ali";

        // Act
        emailService.sendPasswordChangeConfirmationEmail(targetEmail, firstName);

        // Assert
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals(targetEmail, Objects.requireNonNull(sentMessage.getTo())[0]);
        assertEquals("Password Changed Successfully - LMS System", sentMessage.getSubject());
        assertTrue(Objects.requireNonNull(sentMessage.getText()).contains("Salam " + firstName));
    }

    @Test
    void shouldSendPasswordChangeConfirmationEmailWithFallbackNameWhenFirstNameIsNull() {
        // Arrange
        String firstName = null;

        // Act
        emailService.sendPasswordChangeConfirmationEmail(targetEmail, firstName);

        // Assert
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertTrue(Objects.requireNonNull(sentMessage.getText()).contains("Salam İstifadəçi"));
    }

    @Test
    void shouldThrowExceptionWhenPasswordChangeConfirmationEmailFails() {
        // Arrange
        doThrow(new MailSendException("SMTP error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(EmailSendingException.class,
                () -> emailService.sendPasswordChangeConfirmationEmail(targetEmail, "Ali"));
    }

    @Test
    void shouldSendRegisterConfirmationEmailSuccessfully() {
        // Arrange
        String firstName = "Aysel";

        // Act
        emailService.sendRegisterConfirmationEmail(targetEmail, firstName);

        // Assert
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals(targetEmail, Objects.requireNonNull(sentMessage.getTo())[0]);
        assertEquals("User Registered Successfully - LMS System", sentMessage.getSubject());
        assertTrue(Objects.requireNonNull(sentMessage.getText()).contains("Salam " + firstName));
    }

    @Test
    void shouldThrowExceptionWhenRegisterConfirmationEmailFails() {
        // Arrange
        doThrow(new RuntimeException("Unexpected error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(EmailSendingException.class,
                () -> emailService.sendRegisterConfirmationEmail(targetEmail, "Aysel"));
    }
}