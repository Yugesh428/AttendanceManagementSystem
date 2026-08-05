package com.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Universal response envelope used by every endpoint.
 *
 * Success shape:
 * {
 *   "success": true,
 *   "status":  201,
 *   "message": "Building created successfully",
 *   "data":    { ... }
 * }
 *
 * Error shape (produced by GlobalExceptionHandler):
 * {
 *   "success": false,
 *   "status":  404,
 *   "message": "Building not found with id = '...'",
 *   "errors":  { "name": "must not be blank" }   ← only on validation failures
 * }
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)   // omit null fields (errors, data) when not needed
public class ApiResponse<T> {

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    private final boolean success;
    private final int     status;
    private final String  message;

    private final T      data;    // payload on success
    private final Object errors;  // field-error map on validation failure

    // ── Static factory helpers ────────────────────────────────────────────────

    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(status)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return success(200, message, data);
    }

    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .status(200)
                .message(message)
                .build();
    }

    public static ApiResponse<Void> error(int status, String message, Object errors) {
        return ApiResponse.<Void>builder()
                .success(false)
                .status(status)
                .message(message)
                .errors(errors)
                .build();
    }
}
