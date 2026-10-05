package com.alejandro.notificationapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad.
 *
 * <p>Stopgap: la API se protege con HTTP Basic usando un único usuario
 * administrador en memoria ({@code app.admin.*}). Cuando se implemente JWT
 * se registra acá el filtro que valida el token (antes de
 * {@code UsernamePasswordAuthenticationFilter}), se cambia la política de
 * sesión ya es STATELESS, y el {@link UserDetailsService} pasa a leer de
 * {@code UserRepository}.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Rutas de documentación (OpenAPI JSON + Swagger UI). */
    private static final String[] DOCS_WHITELIST = {
        "/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml",
        "/swagger-ui.html", "/swagger-ui/**"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(DOCS_WHITELIST).permitAll()
                // Registro de usuarios abierto; el resto requiere autenticación.
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(
        PasswordEncoder passwordEncoder,
        @Value("${app.admin.username:admin}") String username,
        @Value("${app.admin.password:admin}") String password) {
        UserDetails admin = User.withUsername(username)
            .password(passwordEncoder.encode(password))
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(admin);
    }
}
