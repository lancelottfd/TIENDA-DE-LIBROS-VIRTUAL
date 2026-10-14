package com.tiendalibros.dto;

public record AuthResponse(
        String token,
        String tipo,
        String username,
        String rol) {

    public static AuthResponse bearer(String token, String username, String rol) {
        return new AuthResponse(token, "Bearer", username, rol);
    }
}
