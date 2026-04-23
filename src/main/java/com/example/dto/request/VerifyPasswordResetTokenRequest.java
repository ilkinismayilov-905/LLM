package com.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record VerifyPasswordResetTokenRequest(
    @NotBlank(message = "Token cannot be blank")
    String token
) {
}

