package com.tiendalibros.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            "clave-secreta-de-pruebas-con-mas-de-32-caracteres-123";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 60_000);
    }

    @Test
    void debeGenerarTokenConUsernameYRol() {
        String token = jwtService.generarToken("admin", "ADMIN");

        assertNotNull(token);
        assertTrue(jwtService.esTokenValido(token));
        assertEquals("admin", jwtService.extraerUsername(token));
        assertEquals("ADMIN", jwtService.extraerRol(token));
    }

    @Test
    void debeRechazarTokenExpirado() {
        JwtService expirado = new JwtService(SECRET, -1_000);

        String token = expirado.generarToken("admin", "ADMIN");

        assertFalse(jwtService.esTokenValido(token));
    }

    @Test
    void debeRechazarTokenFirmadoConOtraClave() {
        JwtService otro = new JwtService(
                "otra-clave-distinta-con-mas-de-32-caracteres-999", 60_000);

        String tokenAjeno = otro.generarToken("admin", "ADMIN");

        assertFalse(jwtService.esTokenValido(tokenAjeno));
    }

    @Test
    void debeRechazarTextoQueNoEsUnToken() {
        assertFalse(jwtService.esTokenValido("esto-no-es-un-jwt"));
        assertFalse(jwtService.esTokenValido(""));
    }
}
