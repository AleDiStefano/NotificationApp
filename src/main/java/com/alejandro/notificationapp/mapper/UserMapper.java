package com.alejandro.notificationapp.mapper;

import com.alejandro.notificationapp.dto.UserResponse;
import com.alejandro.notificationapp.model.User;
import org.springframework.stereotype.Component;

/**
 * Conversión entre la entidad {@link User} y sus DTOs.
 *
 * <p>Vive entre la capa de servicio y la de presentación; mantiene a las
 * entidades libres de conocimiento sobre la API.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getPhoneNumber(),
            user.getPushToken(),
            user.getRole(),
            user.isEnabled(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
