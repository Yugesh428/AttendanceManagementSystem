package com.exception;

import org.springframework.http.HttpStatus;

/**
 * 404 – resource not found (e.g. SuperAdmin / Admin not found by id or email).
 */
public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(HttpStatus.NOT_FOUND, resource + " not found with " + field + " = '" + value + "'");
    }
}
