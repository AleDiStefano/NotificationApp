package com.alejandro.notificationapp;

import static org.assertj.core.api.Assertions.assertThat;

import com.alejandro.notificationapp.dto.CreateNotificationRequest;
import com.alejandro.notificationapp.dto.CreateUserRequest;
import com.alejandro.notificationapp.model.NotificationChannel;
import com.alejandro.notificationapp.model.NotificationStatus;
import com.alejandro.notificationapp.repository.NotificationRepository;
import com.alejandro.notificationapp.service.NotificationService;
import com.alejandro.notificationapp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.awaitility.Awaitility.await;

/**
 * Prueba de punta a punta del pipeline RabbitMQ: crear una notificación
 * EMAIL debe terminar en {@code status = SENT} y el correo debe llegar
 * realmente a un servidor SMTP (MailHog, levantado como container de test).
 */
@Testcontainers
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotificationDeliveryIntegrationTest {

    @Container
    static GenericContainer<?> mailhog =
        new GenericContainer<>(DockerImageName.parse("mailhog/mailhog:v1.0.1"))
            .withExposedPorts(1025, 8025);

    @DynamicPropertySource
    static void mailProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", mailhog::getHost);
        registry.add("spring.mail.port", () -> mailhog.getMappedPort(1025));
    }

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void creatingEmailNotification_deliversThroughRabbitAndReachesMailhog() throws Exception {
        String email = "delivery-" + UUID.randomUUID() + "@example.com";
        var user = userService.create(new CreateUserRequest(email, "Delivery Test User", "password123", null, null));

        String subject = "Asunto de prueba " + UUID.randomUUID();
        var notification = notificationService.create(
            new CreateNotificationRequest(user.id(), subject, "Cuerpo del mensaje de prueba", NotificationChannel.EMAIL));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            var updated = notificationRepository.findById(notification.id()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(NotificationStatus.SENT);
        });

        RestClient restClient = RestClient.builder()
            .baseUrl("http://" + mailhog.getHost() + ":" + mailhog.getMappedPort(8025))
            .build();

        // MailHog responde con Content-Type "text/json" (no estándar), que ningún
        // HttpMessageConverter registrado reconoce como JSON; se lee como texto y
        // se parsea manualmente para evitar el UnknownContentTypeException.
        String rawBody = restClient.get()
            .uri("/api/v2/messages")
            .retrieve()
            .body(String.class);

        Map<String, Object> messages = new ObjectMapper().readValue(rawBody, Map.class);

        assertThat(messages).isNotNull();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) messages.get("items");
        assertThat(items).isNotEmpty();

        boolean subjectFound = items.stream().anyMatch(item -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) item.get("Content");
            @SuppressWarnings("unchecked")
            Map<String, Object> headers = (Map<String, Object>) content.get("Headers");
            @SuppressWarnings("unchecked")
            List<String> subjects = (List<String>) headers.get("Subject");
            return subjects != null && subjects.contains(subject);
        });
        assertThat(subjectFound).isTrue();
    }
}
