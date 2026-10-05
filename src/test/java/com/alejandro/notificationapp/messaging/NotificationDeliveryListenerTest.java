package com.alejandro.notificationapp.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alejandro.notificationapp.model.Notification;
import com.alejandro.notificationapp.model.NotificationChannel;
import com.alejandro.notificationapp.model.NotificationStatus;
import com.alejandro.notificationapp.model.User;
import com.alejandro.notificationapp.repository.NotificationRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class NotificationDeliveryListenerTest {

    private static final String MAIL_FROM = "no-reply@notificationapp.local";

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private JavaMailSender mailSender;

    private NotificationDeliveryListener listener;

    @BeforeEach
    void setUp() {
        listener = new NotificationDeliveryListener(notificationRepository, mailSender, MAIL_FROM);
    }

    @Test
    void emailChannel_whenMailSendsSuccessfully_marksNotificationSent() {
        Notification notification = notificationOf(NotificationChannel.EMAIL);
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));

        listener.onMessage(new NotificationMessage(notification.getId()));

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void emailChannel_whenMailSendFails_propagatesExceptionAndDoesNotMarkSent() {
        Notification notification = notificationOf(NotificationChannel.EMAIL);
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));
        doThrow(new MailSendException("smtp down")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> listener.onMessage(new NotificationMessage(notification.getId())))
            .isInstanceOf(MailSendException.class);

        assertThat(notification.getStatus()).isNotEqualTo(NotificationStatus.SENT);
    }

    @Test
    void alreadySentNotification_isNotReprocessedOnRedelivery() {
        Notification notification = notificationOf(NotificationChannel.EMAIL);
        notification.changeStatus(NotificationStatus.SENT);
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));

        listener.onMessage(new NotificationMessage(notification.getId()));

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void smsChannel_marksFailedWithoutInvokingMailSenderOrThrowing() {
        Notification notification = notificationOf(NotificationChannel.SMS);
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));

        listener.onMessage(new NotificationMessage(notification.getId()));

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    private Notification notificationOf(NotificationChannel channel) {
        User recipient = new User("user@example.com", "Test User", "hashed-password");
        Notification notification = new Notification(recipient, "Asunto", "Mensaje", channel);
        setId(notification, UUID.randomUUID());
        setId(recipient, UUID.randomUUID());
        return notification;
    }

    private void setId(Object entity, UUID id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
