package com.alejandro.notificationapp;

import static org.assertj.core.api.Assertions.assertThat;

import com.alejandro.notificationapp.messaging.NotificationFailureRecoverer;
import com.alejandro.notificationapp.messaging.NotificationMessage;
import com.alejandro.notificationapp.model.Notification;
import com.alejandro.notificationapp.model.NotificationChannel;
import com.alejandro.notificationapp.model.NotificationStatus;
import com.alejandro.notificationapp.model.User;
import com.alejandro.notificationapp.repository.NotificationRepository;
import com.alejandro.notificationapp.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Verifica con un contexto Spring real (no Mockito) que
 * {@link NotificationFailureRecoverer} efectivamente persiste el estado
 * {@code FAILED} en la base cuando se agotan los reintentos.
 *
 * <p>Este test existe puntualmente porque un bug anterior hacía que
 * {@code @Transactional} fuera un no-op por self-invocation dentro de la
 * misma clase: con mocks (Mockito puro) ese bug no se detecta, porque no hay
 * proxy de Spring de por medio. Sólo un test contra el contexto real lo
 * expone.
 *
 * <p>La notificación se inserta directamente por repositorio (no vía
 * {@code NotificationService.create}) para no publicar el evento de dominio
 * real: eso haría que el {@code NotificationDeliveryListener} real la
 * consumiera en paralelo y compitiera con la llamada explícita al
 * recoverer que hace este test.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class NotificationFailureRecovererIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationFailureRecoverer notificationFailureRecoverer;

    @Autowired
    private Jackson2JsonMessageConverter messageConverter;

    @Test
    void recover_persistsFailedStatus_throughRealTransactionalProxy() {
        User user = userRepository.saveAndFlush(
            new User("recoverer-" + UUID.randomUUID() + "@example.com", "Recoverer Test User", "hashed-password"));
        Notification notification = notificationRepository.saveAndFlush(
            new Notification(user, "Asunto", "Mensaje", NotificationChannel.EMAIL));

        Message message = messageConverter.toMessage(
            new NotificationMessage(notification.getId()), new MessageProperties());

        notificationFailureRecoverer.recover(message, new RuntimeException("fallo simulado agotando reintentos"));

        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(NotificationStatus.FAILED);
    }
}
