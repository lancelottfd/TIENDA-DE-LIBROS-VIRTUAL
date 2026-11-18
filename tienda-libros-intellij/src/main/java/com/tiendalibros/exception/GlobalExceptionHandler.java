package com.tiendalibros.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce las excepciones a respuestas JSON {"mensaje": "..."} con el código HTTP correcto:
 * 400 (datos inválidos), 401 (credenciales), 404 (no existe), 409 (conflicto) y 500.
 * (401 sin token y 403 sin permisos los resuelve SecurityConfig.)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 / 500 genérico
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> manejarError(RuntimeException ex) {

        if ("Libro no encontrado".equals(ex.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", ex.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error interno del servidor"));
    }

    // 400: reglas de negocio (p. ej. "Stock insuficiente")
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarSolicitudInvalida(
            IllegalArgumentException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", ex.getMessage()));
    }

    // 400: @Valid falló. Devuelve el detalle por campo.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("mensaje", "Hay datos inválidos en la solicitud");
        cuerpo.put("errores", errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cuerpo);
    }

    // 400: JSON mal formado o parámetro con tipo incorrecto
    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> manejarFormatoInvalido(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", "La solicitud tiene un formato inválido"));
    }

    // 401: credenciales incorrectas en /api/auth/login
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> manejarCredencialesInvalidas(
            AuthenticationException ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("mensaje", "Usuario o contraseña incorrectos"));
    }

    // 409: conflicto (ISBN duplicado, etc.)
    @ExceptionHandler({ConflictoException.class, DataIntegrityViolationException.class})
    public ResponseEntity<Map<String, String>> manejarConflicto(RuntimeException ex) {
        String mensaje = (ex instanceof ConflictoException)
                ? ex.getMessage()
                : "La operación entra en conflicto con datos existentes";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensaje", mensaje));
    }
}
