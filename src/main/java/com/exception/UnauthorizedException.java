package com.exception;

import org.springframework.http.HttpStatus;

/**
 * 401 – invalid credentials or inactive account.
 */
public class UnauthorizedException extends AppException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
