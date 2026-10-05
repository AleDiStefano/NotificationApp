package com.alejandro.notificationapp.exception;

/**
 * Se lanza cuando se intenta crear un recurso que viola una restricción
 * de unicidad (por ejemplo, un email ya registrado). Se traduce a HTTP 409.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
