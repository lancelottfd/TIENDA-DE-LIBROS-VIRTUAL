package com.tiendalibros.exception;

/** Se lanza cuando la operación choca con el estado actual (p. ej. ISBN duplicado) -> HTTP 409. */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
