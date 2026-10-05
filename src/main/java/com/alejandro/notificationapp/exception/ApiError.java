package com.alejandro.notificationapp.exception;

import java.time.Instant;
import java.util.List;

/**
 * Cuerpo de error uniforme para todas las respuestas 4xx/5xx de la API.
 */
public record ApiError(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    List<FieldViolation> fieldErrors
) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, List.of());
    }

    public static ApiError of(int status, String error, String message, String path, List<FieldViolation> fieldErrors) {
        return new ApiError(Instant.now(), status, error, message, path, fieldErrors);
    }

    /** Detalle de un campo que no pasó validación. */
    public record FieldViolation(String field, String message) {}
}
