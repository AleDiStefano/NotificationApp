package com.alejandro.notificationapp.messaging;

import com.alejandro.notificationapp.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Component;

/**
 * Se invoca cuando el listener de entrega agota sus reintentos
 * ({@code spring.rabbitmq.listener.simple.retry.max-retries}). Marca la
 * notificación como {@code FAILED} de forma definitiva (delegando la parte
 * transaccional en {@link NotificationFailureService}, un bean separado para
 * que el proxy de Spring intercepte la llamada de verdad) y republica el
 * mensaje original a la dead-letter queue para que el fallo quede
 * auditable (headers {@code x-exception-message}, etc.).
 */
@Component
public class NotificationFailureRecoverer implements MessageRecoverer {

    private static final Logger log = LoggerFactory.getLogger(NotificationFailureRecoverer.class);

    private final NotificationFailureService notificationFailureService;
    private final Jackson2JsonMessageConverter messageConverter;
    private final RepublishMessageRecoverer republishRecoverer;

    public NotificationFailureRecoverer(
        NotificationFailureService notificationFailureService,
        RabbitTemplate rabbitTemplate,
        Jackson2JsonMessageConverter messageConverter) {
        this.notificationFailureService = notificationFailureService;
        this.messageConverter = messageConverter;
        this.republishRecoverer = new RepublishMessageRecoverer(
            rabbitTemplate, RabbitMqConfig.DLX, RabbitMqConfig.DLQ_ROUTING_KEY);
    }

    
    @Override
    public void recover(Message message, Throwable cause) {
        NotificationMessage payload;
        try {
            payload = (NotificationMessage) messageConverter.fromMessage(message);
        } catch (Exception e) {
            log.error("No se pudo deserializar el mensaje fallido para marcarlo FAILED", e);
            republishRecoverer.recover(message, cause);
            return;
        }

        notificationFailureService.markAsFailed(payload.notificationId(), cause);
        republishRecoverer.recover(message, cause);
    }
}
