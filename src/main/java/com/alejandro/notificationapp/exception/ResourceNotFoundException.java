package com.alejandro.notificationapp.exception;

/**
 * Se lanza cuando no existe el recurso pedido. La traduce a HTTP 404
 * {@code GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException("%s no encontrado: %s".formatted(resource, id));
    }
}
