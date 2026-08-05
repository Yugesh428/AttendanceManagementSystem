package com.exception;

import org.springframework.http.HttpStatus;

/**
 * 422 Unprocessable Entity – thrown when an uploaded Excel file
 * contains invalid or unreadable data.
 */
public class ExcelImportException extends AppException {

    public ExcelImportException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
