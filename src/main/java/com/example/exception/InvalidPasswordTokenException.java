package com.example.exception;

public class InvalidPasswordTokenException extends RuntimeException {
    public InvalidPasswordTokenException(String message) {
        super(message);
    }

    public InvalidPasswordTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}

