package com.alejandro.notificationapp.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología RabbitMQ para la entrega asincrónica de notificaciones.
 *
 * <p>Los mensajes se publican al exchange principal con routing key
 * {@code notification.<canal>} y llegan a una única cola de entrega.
 * Cuando el listener agota sus reintentos, el mensaje se republica al
 * exchange de dead-letter para quedar auditable en la DLQ.
 */
@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "notification.exchange";
    public static final String QUEUE = "notification.delivery.queue";
    public static final String ROUTING_KEY_PATTERN = "notification.#";

    public static final String DLX = "notification.dlx";
    public static final String DLQ = "notification.delivery.dlq";
    public static final String DLQ_ROUTING_KEY = "notification.delivery.failed";

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationDeliveryQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding notificationDeliveryBinding() {
        return BindingBuilder.bind(notificationDeliveryQueue())
            .to(notificationExchange())
            .with(ROUTING_KEY_PATTERN);
    }

    @Bean
    public TopicExchange notificationDeadLetterExchange() {
        return new TopicExchange(DLX, true, false);
    }

    @Bean
    public Queue notificationDeadLetterQueue() {
        return new Queue(DLQ, true);
    }

    @Bean
    public Binding notificationDeadLetterBinding() {
        return BindingBuilder.bind(notificationDeadLetterQueue())
            .to(notificationDeadLetterExchange())
            .with(DLQ_ROUTING_KEY);
    }

    /**
     * Spring Boot detecta este bean y lo usa automáticamente como
     * {@code MessageConverter} tanto para {@code RabbitTemplate} como para
     * los contenedores {@code @RabbitListener}; no hace falta redefinirlos.
     */
    // TODO: Jackson2JsonMessageConverter está deprecado "for removal" en Spring AMQP
    // 4.1.1 (reemplazo: JacksonJsonMessageConverter, basado en Jackson 3 / tools.jackson).
    // No es solo cosmético: hay riesgo real de que deje de compilar en una futura
    // versión menor de Spring AMQP 4.x. No migré todavía porque el reemplazo usa un
    // módulo Jackson distinto (tools.jackson) al que usa el resto de la app para JSON
    // HTTP (com.fasterxml.jackson vía Spring MVC/springdoc), y migrar es una decisión
    // de mayor alcance (evaluar si conviene convivir con dos Jacksons o unificar).
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        // Se declara explícitamente el paquete propio como "trusted": por defecto
        // DefaultJackson2JavaTypeMapper sólo confía en java.util/java.lang, y
        // NotificationFailureRecoverer necesita deserializar el mensaje fallido
        // (fromMessage por header __TypeId__) fuera del flujo de @RabbitListener,
        // que sí infiere el tipo directamente del parámetro del método y por eso
        // no pisa esta validación.
        return new Jackson2JsonMessageConverter("com.alejandro.notificationapp.messaging");
    }
}
