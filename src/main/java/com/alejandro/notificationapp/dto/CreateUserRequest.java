package com.alejandro.notificationapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada para registrar un usuario.
 */
public record CreateUserRequest(

    @NotBlank @Email @Size(max = 320)
    String email,

    @NotBlank @Size(max = 120)
    String fullName,

    @NotBlank @Size(min = 8, max = 72)
    String password,

    @Size(max = 30)
    String phoneNumber,

    @Size(max = 512)
    String pushToken
) {}
