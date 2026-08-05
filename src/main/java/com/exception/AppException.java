package com.exception;

import org.springframework.http.HttpStatus;

/**
 * Base application exception that carries an HTTP status.
 * Throw this (or its subclasses) instead of RuntimeException
 * so the GlobalExceptionHandler can return the correct HTTP code.
 */
public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
