package com.alejandro.notificationapp.service;

import com.alejandro.notificationapp.dto.CreateUserRequest;
import com.alejandro.notificationapp.dto.UpdateUserRequest;
import com.alejandro.notificationapp.dto.UserResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Casos de uso sobre usuarios. La capa de presentación depende de esta
 * interfaz, no de su implementación.
 */
public interface UserService {

    UserResponse create(CreateUserRequest request);

    UserResponse getById(UUID id);

    Page<UserResponse> list(Pageable pageable);

    UserResponse update(UUID id, UpdateUserRequest request);

    void delete(UUID id);
}
