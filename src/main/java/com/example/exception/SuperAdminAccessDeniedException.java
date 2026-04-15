package com.example.exception;

public class SuperAdminAccessDeniedException extends RuntimeException {

    public SuperAdminAccessDeniedException(String message) {
        super(message);
    }
}