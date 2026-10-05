package com.alejandro.notificationapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatos de la API para OpenAPI / Swagger UI.
 *
 * <p>Declara el esquema HTTP Basic para que el botón «Authorize» de
 * Swagger UI permita probar los endpoints protegidos (admin/admin por
 * defecto). {@code POST /api/users} es público.
 */
@Configuration
public class OpenApiConfig {

    private static final String BASIC_AUTH = "basicAuth";

    @Bean
    OpenAPI notificationappOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Notification App API")
                .version("v1")
                .description("CRUD de usuarios y notificaciones."))
            .components(new Components().addSecuritySchemes(BASIC_AUTH,
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("basic")))
            .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH));
    }
}
