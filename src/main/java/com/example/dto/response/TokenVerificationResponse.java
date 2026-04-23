package com.example.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenVerificationResponse(
    @JsonProperty("message")
    String message,

    @JsonProperty("is_valid")
    Boolean isValid
) {
    public static TokenVerificationResponse valid(String message) {
        return new TokenVerificationResponse(message, true);
    }

    public static TokenVerificationResponse invalid(String message) {
        return new TokenVerificationResponse(message, false);
    }
}

