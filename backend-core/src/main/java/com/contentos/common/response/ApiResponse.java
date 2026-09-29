package com.contentos.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * WHAT IT DOES:
 * Standard API response wrapper for ALL endpoints in the application.
 *
 * WHY IT EXISTS:
 * Consistent response format makes the frontend code cleaner — it always
 * knows what shape to expect. Also makes error handling uniform.
 *
 * Example success:
 * { "success": true, "data": {...}, "error": null }
 *
 * Example error:
 * { "success": false, "data": null, "error": { "code": "NOT_FOUND", "message": "..." } }
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ApiError error;

    @Builder.Default
    private long timestamp = Instant.now().toEpochMilli();

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .error(ApiError.builder().code(code).message(message).build())
            .build();
    }

    @Data
    @Builder
    public static class ApiError {
        private String code;
        private String message;
    }
}
