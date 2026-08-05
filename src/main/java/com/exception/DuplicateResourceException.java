package com.exception;

import org.springframework.http.HttpStatus;

/**
 * 409 – duplicate / already-exists (e.g. email already taken).
 */
public class DuplicateResourceException extends AppException {

    public DuplicateResourceException(String resource, String field, Object value) {
        super(HttpStatus.CONFLICT, resource + " already exists with " + field + " = '" + value + "'");
    }
}
