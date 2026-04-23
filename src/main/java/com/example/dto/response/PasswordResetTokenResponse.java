package com.example.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PasswordResetTokenResponse(
    @JsonProperty("message")
    String message,

    @JsonProperty("success")
    Boolean success
) {
    public static PasswordResetTokenResponse success(String message) {
        return new PasswordResetTokenResponse(message, true);
    }

    public static PasswordResetTokenResponse failure(String message) {
        return new PasswordResetTokenResponse(message, false);
    }
}

