package com.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ResetPasswordRequest(
    @NotBlank(message = "Token cannot be blank")
    String token,

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String newPassword,

    @NotBlank(message = "Confirm password cannot be blank")
    String confirmPassword
) {
}

