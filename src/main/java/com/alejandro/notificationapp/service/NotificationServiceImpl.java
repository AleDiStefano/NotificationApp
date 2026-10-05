package com.alejandro.notificationapp.service;

import com.alejandro.notificationapp.dto.CreateNotificationRequest;
import com.alejandro.notificationapp.dto.NotificationResponse;
import com.alejandro.notificationapp.exception.ResourceNotFoundException;
import com.alejandro.notificationapp.mapper.NotificationMapper;
import com.alejandro.notificationapp.messaging.NotificationCreatedEvent;
import com.alejandro.notificationapp.model.Notification;
import com.alejandro.notificationapp.model.NotificationChannel;
import com.alejandro.notificationapp.model.NotificationStatus;
import com.alejandro.notificationapp.model.User;
import com.alejandro.notificationapp.repository.NotificationRepository;
import com.alejandro.notificationapp.repository.UserRepository;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationServiceImpl(
        NotificationRepository notificationRepository,
        UserRepository userRepository,
        NotificationMapper notificationMapper,
        ApplicationEventPublisher eventPublisher) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationMapper = notificationMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
        User recipient = userRepository.findById(request.recipientId())
            .orElseThrow(() -> ResourceNotFoundException.of("Usuario destinatario", request.recipientId()));
        Notification notification = new Notification(recipient, request.subject(), request.message(), request.channel());
        Notification saved = notificationRepository.saveAndFlush(notification);
        eventPublisher.publishEvent(new NotificationCreatedEvent(saved.getId(), saved.getChannel()));
        return notificationMapper.toResponse(saved);
    }

    @Override
    public NotificationResponse getById(UUID id) {
        return notificationMapper.toResponse(findOrThrow(id));
    }

    @Override
    public Page<NotificationResponse> list(
        UUID recipientId, NotificationStatus status, NotificationChannel channel, Pageable pageable) {
        return notificationRepository.search(recipientId, status, channel, pageable)
            .map(notificationMapper::toResponse);
    }

    @Override
    @Transactional
    public NotificationResponse updateStatus(UUID id, NotificationStatus status) {
        Notification notification = findOrThrow(id);
        notification.changeStatus(status);
        return notificationMapper.toResponse(notificationRepository.saveAndFlush(notification));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        notificationRepository.delete(findOrThrow(id));
    }

    private Notification findOrThrow(UUID id) {
        return notificationRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Notificación", id));
    }
}
