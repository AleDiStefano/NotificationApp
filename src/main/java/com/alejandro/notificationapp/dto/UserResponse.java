package com.alejandro.notificationapp.dto;

import com.alejandro.notificationapp.model.Role;
import java.time.Instant;
import java.util.UUID;

/**
 * Vista pública de un usuario. Nunca incluye la contraseña.
 */
public record UserResponse(
    UUID id,
    String email,
    String fullName,
    String phoneNumber,
    String pushToken,
    Role role,
    boolean enabled,
    Instant createdAt,
    Instant updatedAt
) {}
