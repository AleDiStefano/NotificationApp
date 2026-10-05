package com.alejandro.notificationapp.messaging;

import com.alejandro.notificationapp.model.Notification;
import com.alejandro.notificationapp.model.NotificationStatus;
import com.alejandro.notificationapp.repository.NotificationRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persiste el marcado definitivo de una notificación como {@code FAILED}.
 *
 * <p>Vive en un bean separado de {@link NotificationFailureRecoverer} a
 * propósito: si {@code @Transactional} estuviera en un método de esa misma
 * clase invocado internamente (self-invocation), el proxy de Spring nunca
 * interceptaría la llamada y la anotación sería un no-op. Al estar en un
 * bean distinto, la llamada pasa por el proxy real y la transacción se
 * aplica de verdad.
 */
@Component
public class NotificationFailureService {

    private static final Logger log = LoggerFactory.getLogger(NotificationFailureService.class);

    private final NotificationRepository notificationRepository;

    public NotificationFailureService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void markAsFailed(UUID notificationId, Throwable cause) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            log.warn(
                "Notificación {} no existe; no se puede marcar FAILED tras agotar reintentos", notificationId);
            return;
        }

        log.error(
            "Notificación {} agotó los reintentos de entrega; se marca FAILED. Causa: {}",
            notification.getId(), cause.getMessage());
        notification.changeStatus(NotificationStatus.FAILED);
        notificationRepository.saveAndFlush(notification);
    }
}
