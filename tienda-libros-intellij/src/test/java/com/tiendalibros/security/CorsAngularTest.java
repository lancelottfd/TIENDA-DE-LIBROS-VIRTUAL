package com.tiendalibros.security;

import com.tiendalibros.controller.AuthController;
import com.tiendalibros.controller.LibroController;
import com.tiendalibros.dto.AuthResponse;
import com.tiendalibros.service.AuthService;
import com.tiendalibros.service.LibroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica la configuración CORS que necesita el frontend Angular
 * (http://localhost:4200) para llamar a la API con el JWT en el header Authorization.
 */
@WebMvcTest(controllers = {LibroController.class, AuthController.class})
@Import({SecurityConfig.class, JwtService.class})
@TestPropertySource(properties = {
        "jwt.secret=" + CorsAngularTest.SECRET,
        "jwt.expiration-ms=60000"
})
class CorsAngularTest {

    static final String SECRET =
            "clave-secreta-de-pruebas-con-mas-de-32-caracteres-123";

    private static final String ORIGEN_ANGULAR = "http://localhost:4200";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LibroService libroService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void preflightDesdeAngularConAuthorizationEsPermitido() throws Exception {
        // El navegador envía esta petición OPTIONS antes de un POST con JWT
        mockMvc.perform(options("/api/libros")
                        .header("Origin", ORIGEN_ANGULAR)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGEN_ANGULAR));
    }

    @Test
    void preflightDeUnOrigenNoPermitidoEsRechazado() throws Exception {
        mockMvc.perform(options("/api/libros")
                        .header("Origin", "http://sitio-no-autorizado.com")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginDesdeAngularIncluyeLaCabeceraCorsEnLaRespuesta() throws Exception {
        when(authService.login(any()))
                .thenReturn(AuthResponse.bearer("token-de-prueba", "admin", "ADMIN"));

        mockMvc.perform(post("/api/auth/login")
                        .header("Origin", ORIGEN_ANGULAR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGEN_ANGULAR));
    }
}
