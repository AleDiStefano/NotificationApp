package com.alejandro.notificationapp.messaging;

import com.alejandro.notificationapp.config.RabbitMqConfig;
import com.alejandro.notificationapp.model.Notification;
import com.alejandro.notificationapp.model.NotificationStatus;
import com.alejandro.notificationapp.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consume los mensajes de entrega y ejecuta el envío real según el canal.
 *
 * <p>Si el envío falla con una excepción transitoria (ej. SMTP caído), se
 * deja propagar para que el contenedor de {@code @RabbitListener} dispare el
 * reintento configurado ({@code spring.rabbitmq.listener.simple.retry.*}).
 * Sólo cuando los reintentos se agotan entra en juego
 * {@link NotificationFailureRecoverer}, que marca la notificación como
 * {@code FAILED} de forma definitiva.
 */
@Component
public class NotificationDeliveryListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryListener.class);

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final String mailFrom;

    public NotificationDeliveryListener(
        NotificationRepository notificationRepository,
        JavaMailSender mailSender,
        @Value("${app.mail.from}") String mailFrom) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    @Transactional
    public void onMessage(NotificationMessage message) {
        Notification notification = notificationRepository.findById(message.notificationId()).orElse(null);
        if (notification == null) {
            log.warn("Notificación {} no existe; se descarta el mensaje", message.notificationId());
            return;
        }

        // Idempotencia ante redelivery: si el ack se perdió después de un commit
        // exitoso (p. ej. cae la conexión justo tras confirmar la transacción),
        // Rabbit puede reentregar el mismo mensaje. No reprocesar algo que ya
        // se envió con éxito, para no duplicar el email.
        if (notification.getStatus() == NotificationStatus.SENT) {
            log.warn(
                "Notificación {} ya está SENT; se descarta la redelivery para evitar reenvío duplicado",
                notification.getId());
            return;
        }

        switch (notification.getChannel()) {
            case EMAIL -> sendEmail(notification);
            case SMS, PUSH -> {
                log.warn(
                    "Canal {} todavía no tiene proveedor configurado; notificación {} marcada FAILED",
                    notification.getChannel(), notification.getId());
                notification.changeStatus(NotificationStatus.FAILED);
                // sin throw: esto no es un error transitorio, reintentar no lo arregla
            }
        }
    }

    private void sendEmail(Notification notification) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(notification.getRecipient().getEmail());
        mailMessage.setFrom(mailFrom);
        mailMessage.setSubject(notification.getSubject());
        mailMessage.setText(notification.getMessage());
        try {
            mailSender.send(mailMessage);
        } catch (MailException e) {
            log.error("Fallo al enviar el email de la notificación {}", notification.getId(), e);
            throw e;
        }
        notification.changeStatus(NotificationStatus.SENT);
    }
}
