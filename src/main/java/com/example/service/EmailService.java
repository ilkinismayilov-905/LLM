package com.example.service;

import com.example.exception.EmailSendingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Send password reset email with verification token
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@lms.com");
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - LMS System");
            message.setText(buildPasswordResetEmailBody(resetLink, resetToken));

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new EmailSendingException("Failed to send password reset email: " + e.getMessage(), e);
        }
    }

    /**
     * Send password change confirmation email
     */
    public void sendPasswordChangeConfirmationEmail(String toEmail, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Password Changed Successfully - LMS System");
            message.setText(buildPasswordChangeConfirmationEmailBody(firstName));

            mailSender.send(message);
            log.info("Password change confirmation email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password change confirmation email to {}: {}", toEmail, e.getMessage());
            throw new EmailSendingException("Failed to send password change confirmation email: " + e.getMessage(), e);
        }
    }

    public void sendRegisterConfirmationEmail(String toEmail, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("User Registered Successfully - LMS System");
            message.setText(buildPasswordChangeConfirmationEmailBody(firstName));

            mailSender.send(message);
            log.info("Register confirmation email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send register confirmation email to {}: {}", toEmail, e.getMessage());
            throw new EmailSendingException("Failed to send register confirmation email: " + e.getMessage(), e);
        }
    }

    private String buildPasswordResetEmailBody(String resetLink, String resetToken) {
        return String.format(
                "Salam,\n\n" +
                "Parolunuzu dəyişmək üçün aşağıdakı linkə klikləyin:\n\n" +
                "%s\n\n" +
                "Doğrulama Kodu: %s\n\n" +
                "Bu link 1 saat ərində etibarlıdır.\n" +
                "Əgər siz bu sorğunu göndərməmişsinizsə, bu e-poçtu görməzliyə gələ biləsiniz.\n\n" +
                "LMS Sistemi\n" +
                "support@lms.com",
                resetLink, resetToken
        );
    }

    private String buildPasswordChangeConfirmationEmailBody(String firstName) {
        return String.format(
                "Salam %s,\n\n" +
                "Parolunuz uğurla dəyişdirilib.\n\n" +
                "Əgər bu dəyişikliyi siz etməmişsinizsə, dərhal emin olun ki, hesabınız təhlükəli.\n" +
                "Lütfən, derhal bizə müraciət edin.\n\n" +
                "Hörmətlə,\n" +
                "LMS Sistemi\n" +
                "support@lms.com",
                firstName != null ? firstName : "İstifadəçi"
        );
    }
}

