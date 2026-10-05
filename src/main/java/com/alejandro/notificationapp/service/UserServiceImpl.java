package com.alejandro.notificationapp.service;

import com.alejandro.notificationapp.dto.CreateUserRequest;
import com.alejandro.notificationapp.dto.UpdateUserRequest;
import com.alejandro.notificationapp.dto.UserResponse;
import com.alejandro.notificationapp.exception.DuplicateResourceException;
import com.alejandro.notificationapp.exception.ResourceNotFoundException;
import com.alejandro.notificationapp.mapper.UserMapper;
import com.alejandro.notificationapp.model.User;
import com.alejandro.notificationapp.repository.UserRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Ya existe un usuario con el email " + request.email());
        }
        User user = new User(request.email(), request.fullName(), passwordEncoder.encode(request.password()));
        user.setPhoneNumber(request.phoneNumber());
        user.setPushToken(request.pushToken());
        return userMapper.toResponse(userRepository.saveAndFlush(user));
    }

    @Override
    public UserResponse getById(UUID id) {
        return userMapper.toResponse(findOrThrow(id));
    }

    @Override
    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = findOrThrow(id);
        user.setFullName(request.fullName());
        user.setPhoneNumber(request.phoneNumber());
        user.setPushToken(request.pushToken());
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }
        return userMapper.toResponse(userRepository.saveAndFlush(user));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        userRepository.delete(findOrThrow(id));
    }

    private User findOrThrow(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Usuario", id));
    }
}
