package com.tiendalibros.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            UserDetailsService userDetailsService) throws Exception {

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(jwtService, userDetailsService);

        http
                // API REST con JWT: sin sesiones ni cookies, por eso CSRF no aplica
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Públicas
                        .requestMatchers("/api/auth/**", "/error").permitAll()

                        // Solo ADMIN (esta regla va ANTES de la de GET /api/libros/**)
                        .requestMatchers(HttpMethod.GET, "/api/libros/stock-bajo")
                        .hasRole("ADMIN")

                        // ADMIN y CLIENTE pueden consultar el catálogo
                        .requestMatchers(HttpMethod.GET, "/api/libros", "/api/libros/**")
                        .hasAnyRole("ADMIN", "CLIENTE")

                        // ADMIN y CLIENTE pueden registrar una salida (compra)
                        .requestMatchers(HttpMethod.POST, "/api/libros/*/salida")
                        .hasAnyRole("ADMIN", "CLIENTE")

                        // Resto de /api/libros (crear, modificar, eliminar): solo ADMIN
                        .requestMatchers("/api/libros/**").hasRole("ADMIN")

                        .anyRequest().authenticated())

                // Respuestas JSON coherentes con el resto de la API
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) ->
                                escribirError(response, HttpStatus.UNAUTHORIZED,
                                        "No autenticado: token ausente o inválido"))
                        .accessDeniedHandler((request, response, e) ->
                                escribirError(response, HttpStatus.FORBIDDEN,
                                        "No tienes permisos para esta operación")))

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private void escribirError(jakarta.servlet.http.HttpServletResponse response,
                               HttpStatus status, String mensaje)
            throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"mensaje\":\"" + mensaje + "\"}");
    }
}
