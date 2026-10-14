package com.tiendalibros.security;

import com.tiendalibros.controller.AuthController;
import com.tiendalibros.controller.LibroController;
import com.tiendalibros.dto.AuthResponse;
import com.tiendalibros.model.Libro;
import com.tiendalibros.model.MovimientoInventario;
import com.tiendalibros.model.Rol;
import com.tiendalibros.service.AuthService;
import com.tiendalibros.service.LibroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba la configuración de Spring Security (roles + JWT) sobre los
 * controladores reales, sin necesidad de base de datos.
 */
@WebMvcTest(controllers = {LibroController.class, AuthController.class})
@Import({SecurityConfig.class, JwtService.class})
@TestPropertySource(properties = {
        "jwt.secret=" + SeguridadRutasTest.SECRET,
        "jwt.expiration-ms=60000"
})
class SeguridadRutasTest {

    static final String SECRET =
            "clave-secreta-de-pruebas-con-mas-de-32-caracteres-123";

    private static final String LIBRO_JSON = """
            {"titulo":"El Principito","autor":"Saint-Exupéry",
             "precio":30.0,"stock":10,"categoriaId":1}""";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private LibroService libroService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    // ---------- helpers ----------

    /** Simula que el usuario existe en BD con ese rol y devuelve su header Bearer. */
    private String bearer(String username, Rol rol) {
        when(userDetailsService.loadUserByUsername(username))
                .thenReturn(new User(username, "hash",
                        List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()))));
        return "Bearer " + jwtService.generarToken(username, rol.name());
    }

    private Libro libro() {
        return new Libro(1L, "El Principito", "Saint-Exupéry", 30.0, 10, 1L);
    }

    // ---------- 401: sin autenticar ----------

    @Test
    void sinTokenDebeRetornar401() throws Exception {
        mockMvc.perform(get("/api/libros"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void tokenInvalidoDebeRetornar401() throws Exception {
        mockMvc.perform(get("/api/libros")
                        .header("Authorization", "Bearer esto.no.es.un.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenExpiradoDebeRetornar401() throws Exception {
        JwtService expirado = new JwtService(SECRET, -1_000);
        String token = expirado.generarToken("admin", "ADMIN");

        mockMvc.perform(get("/api/libros")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenDeUsuarioInexistenteDebeRetornar401() throws Exception {
        when(userDetailsService.loadUserByUsername("fantasma"))
                .thenThrow(new UsernameNotFoundException("no existe"));
        String token = jwtService.generarToken("fantasma", "ADMIN");

        mockMvc.perform(get("/api/libros")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    // ---------- CLIENTE ----------

    @Test
    void clientePuedeListarYObtenerLibros() throws Exception {
        when(libroService.listar()).thenReturn(List.of(libro()));
        when(libroService.obtenerPorId(1L)).thenReturn(libro());
        String auth = bearer("cliente", Rol.CLIENTE);

        mockMvc.perform(get("/api/libros").header("Authorization", auth))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/libros/1").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("El Principito"));
    }

    @Test
    void clientePuedeRegistrarSalida() throws Exception {
        when(libroService.registrarSalida(1L, 2))
                .thenReturn(new MovimientoInventario(libro(), 2, "SALIDA"));

        mockMvc.perform(post("/api/libros/1/salida")
                        .param("cantidad", "2")
                        .header("Authorization", bearer("cliente", Rol.CLIENTE)))
                .andExpect(status().isCreated());
    }

    @Test
    void clienteNoPuedeCrearLibro_403() throws Exception {
        mockMvc.perform(post("/api/libros")
                        .header("Authorization", bearer("cliente", Rol.CLIENTE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LIBRO_JSON))
                .andExpect(status().isForbidden());

        verify(libroService, never()).crear(any());
    }

    @Test
    void clienteNoPuedeModificarLibro_403() throws Exception {
        mockMvc.perform(put("/api/libros/1")
                        .header("Authorization", bearer("cliente", Rol.CLIENTE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LIBRO_JSON))
                .andExpect(status().isForbidden());

        verify(libroService, never()).actualizar(any(), any());
    }

    @Test
    void clienteNoPuedeEliminarLibro_403() throws Exception {
        mockMvc.perform(delete("/api/libros/1")
                        .header("Authorization", bearer("cliente", Rol.CLIENTE)))
                .andExpect(status().isForbidden());

        verify(libroService, never()).eliminar(any());
    }

    @Test
    void clienteNoPuedeVerStockBajo_403() throws Exception {
        mockMvc.perform(get("/api/libros/stock-bajo")
                        .param("cantidad", "5")
                        .header("Authorization", bearer("cliente", Rol.CLIENTE)))
                .andExpect(status().isForbidden());
    }

    // ---------- ADMIN ----------

    @Test
    void adminPuedeCrearLibro_201() throws Exception {
        when(libroService.crear(any(Libro.class))).thenReturn(libro());

        mockMvc.perform(post("/api/libros")
                        .header("Authorization", bearer("admin", Rol.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LIBRO_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void adminPuedeModificarLibro_200() throws Exception {
        when(libroService.actualizar(eq(1L), any(Libro.class))).thenReturn(libro());

        mockMvc.perform(put("/api/libros/1")
                        .header("Authorization", bearer("admin", Rol.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LIBRO_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void adminPuedeEliminarLibro_204() throws Exception {
        mockMvc.perform(delete("/api/libros/1")
                        .header("Authorization", bearer("admin", Rol.ADMIN)))
                .andExpect(status().isNoContent());

        verify(libroService).eliminar(1L);
    }

    @Test
    void adminPuedeVerStockBajo_200() throws Exception {
        when(libroService.buscarLibrosConStockBajo(5)).thenReturn(List.of(libro()));

        mockMvc.perform(get("/api/libros/stock-bajo")
                        .param("cantidad", "5")
                        .header("Authorization", bearer("admin", Rol.ADMIN)))
                .andExpect(status().isOk());
    }

    // ---------- /api/auth (rutas públicas) ----------

    @Test
    void loginEsPublicoYDevuelveToken() throws Exception {
        when(authService.login(any()))
                .thenReturn(AuthResponse.bearer("token-de-prueba", "admin", "ADMIN"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-de-prueba"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    void loginConCredencialesIncorrectasDebeRetornar401() throws Exception {
        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("mal"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void loginConCuerpoVacioDebeRetornar400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registroEsPublicoYDevuelve201() throws Exception {
        when(authService.registrar(any()))
                .thenReturn(AuthResponse.bearer("t", "maria", "CLIENTE"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"maria\",\"password\":\"secreto123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }
}
