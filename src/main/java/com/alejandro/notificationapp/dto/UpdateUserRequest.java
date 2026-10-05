package com.alejandro.notificationapp.dto;

import com.alejandro.notificationapp.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada para actualizar un usuario. El email y la contraseña
 * se cambian por endpoints propios (no acá).
 */
public record UpdateUserRequest(

    @NotBlank @Size(max = 120)
    String fullName,

    @Size(max = 30)
    String phoneNumber,

    @Size(max = 512)
    String pushToken,

    Role role,

    Boolean enabled
) {}
