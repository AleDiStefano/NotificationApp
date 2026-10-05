package com.alejandro.notificationapp.controller;

import com.alejandro.notificationapp.dto.CreateNotificationRequest;
import com.alejandro.notificationapp.dto.NotificationResponse;
import com.alejandro.notificationapp.dto.UpdateNotificationStatusRequest;
import com.alejandro.notificationapp.model.NotificationChannel;
import com.alejandro.notificationapp.model.NotificationStatus;
import com.alejandro.notificationapp.service.NotificationService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Capa Controller (MVC): expone el CRUD de notificaciones sobre HTTP.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> create(
        @Valid @RequestBody CreateNotificationRequest request, UriComponentsBuilder uriBuilder) {
        NotificationResponse created = notificationService.create(request);
        URI location = uriBuilder.path("/api/notifications/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public NotificationResponse getById(@PathVariable UUID id) {
        return notificationService.getById(id);
    }

    @GetMapping
    public Page<NotificationResponse> list(
        @RequestParam(required = false) UUID recipientId,
        @RequestParam(required = false) NotificationStatus status,
        @RequestParam(required = false) NotificationChannel channel,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return notificationService.list(recipientId, status, channel, pageable);
    }

    @PatchMapping("/{id}/status")
    public NotificationResponse updateStatus(
        @PathVariable UUID id, @Valid @RequestBody UpdateNotificationStatusRequest request) {
        return notificationService.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
