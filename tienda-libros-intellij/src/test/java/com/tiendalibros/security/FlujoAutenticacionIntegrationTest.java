package com.tiendalibros.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba de extremo a extremo con la BD real de pruebas (MySQL,
 * igual que LibroRollbackIntegrationTest): login real, token real,
 * usuarios sembrados por DataInitializer.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FlujoAutenticacionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String login(String username, String password) throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"username\":\"%s\",\"password\":\"%s\"}",
                                username, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(respuesta);
        String token = json.get("token").asText();
        assertFalse(token.isBlank());
        return "Bearer " + token;
    }

    @Test
    void loginConCredencialesIncorrectasDebeRetornar401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rutaProtegidaSinTokenDebeRetornar401() throws Exception {
        mockMvc.perform(get("/api/libros"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCreaLibroYClienteNoPuedeEliminarlo() throws Exception {
        String admin = login("admin", "admin123");
        String cliente = login("cliente", "cliente123");

        // ADMIN crea
        String creado = mockMvc.perform(post("/api/libros")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Libro seguridad\",\"autor\":\"Autor\","
                                + "\"precio\":10.0,\"stock\":5,\"categoriaId\":1}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(creado).get("id").asLong();

        // CLIENTE puede verlo...
        mockMvc.perform(get("/api/libros/" + id).header("Authorization", cliente))
                .andExpect(status().isOk());

        // ...pero no eliminarlo
        mockMvc.perform(delete("/api/libros/" + id).header("Authorization", cliente))
                .andExpect(status().isForbidden());

        // ADMIN sí
        mockMvc.perform(delete("/api/libros/" + id).header("Authorization", admin))
                .andExpect(status().isNoContent());
    }

    @Test
    void registroPublicoCreaClienteSinPermisosDeAdmin() throws Exception {
        String usuario = "nuevo" + System.currentTimeMillis();

        String respuesta = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + usuario
                                + "\",\"password\":\"secreto123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andReturn().getResponse().getContentAsString();

        String token = "Bearer " + objectMapper.readTree(respuesta).get("token").asText();

        mockMvc.perform(get("/api/libros").header("Authorization", token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/libros")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"x\",\"autor\":\"y\",\"precio\":1,\"stock\":1}"))
                .andExpect(status().isForbidden());
    }
}
