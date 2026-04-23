package com.example.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChangePasswordResponse(
    @JsonProperty("message")
    String message,

    @JsonProperty("success")
    Boolean success
) {
    public static ChangePasswordResponse success(String message) {
        return new ChangePasswordResponse(message, true);
    }

    public static ChangePasswordResponse failure(String message) {
        return new ChangePasswordResponse(message, false);
    }
}

