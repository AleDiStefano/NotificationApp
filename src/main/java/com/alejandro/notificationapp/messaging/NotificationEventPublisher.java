package com.alejandro.notificationapp.messaging;

import com.alejandro.notificationapp.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publica a RabbitMQ una vez confirmada la transacción que creó la
 * notificación (ver {@code NotificationServiceImpl#create}). Publicar antes
 * del commit arriesgaría anunciar una notificación que termine en rollback.
 *
 * <p><b>Deuda técnica conocida (sin outbox/reconciliación):</b> si el broker
 * está caído justo en este {@code AFTER_COMMIT}, o la aplicación crashea
 * entre el commit de la fila (que queda {@code PENDING}) y la ejecución de
 * este listener, el evento se pierde para siempre y la notificación se
 * queda en {@code PENDING} sin que nada la reintente — no hay garantía de
 * entrega "at least once" real. La solución correcta a futuro es un patrón
 * outbox transaccional (guardar el evento en la misma transacción que la
 * fila y tener un publisher separado que lo lea y confirme), o en su
 * defecto un job periódico de reconciliación que detecte notificaciones
 * {@code PENDING} más viejas que cierto umbral y las vuelva a publicar.
 */
@Component
public class NotificationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public NotificationEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationCreated(NotificationCreatedEvent event) {
        String routingKey = "notification." + event.channel().name().toLowerCase();
        log.debug("Publicando notificación {} a routing key {}", event.notificationId(), routingKey);
        rabbitTemplate.convertAndSend(
            RabbitMqConfig.EXCHANGE, routingKey, new NotificationMessage(event.notificationId()));
    }
}
