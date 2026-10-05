package com.alejandro.notificationapp.repository;

import com.alejandro.notificationapp.model.Notification;
import com.alejandro.notificationapp.model.NotificationChannel;
import com.alejandro.notificationapp.model.NotificationStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Capa de acceso a datos para {@link Notification}.
 */
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Búsqueda paginada con filtros opcionales (cualquier parámetro null se ignora).
     */
    @Query("""
        select n from Notification n
        where (:recipientId is null or n.recipient.id = :recipientId)
          and (:status is null or n.status = :status)
          and (:channel is null or n.channel = :channel)
        """)
    Page<Notification> search(
        @Param("recipientId") UUID recipientId,
        @Param("status") NotificationStatus status,
        @Param("channel") NotificationChannel channel,
        Pageable pageable);
}
