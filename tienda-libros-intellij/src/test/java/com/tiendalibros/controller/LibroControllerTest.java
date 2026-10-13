package com.tiendalibros.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiendalibros.model.Libro;
import com.tiendalibros.repository.LibroRepository;
import com.tiendalibros.service.LibroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LibroControllerTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        LibroRepository repository = new LibroRepository();
        LibroService service = new LibroService(repository);
        LibroController controller = new LibroController(service);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new com.tiendalibros.exception.GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void postDebeCrearLibro() throws Exception {
        Libro libro = new Libro(
                null,
                "Cien años de soledad",
                "Gabriel García Márquez",
                45.0,
                10,
                1L
        );

        mockMvc.perform(
                post("/api/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(libro))
        ).andExpect(status().isCreated());
    }

    @Test
    void getDebeRetornar404CuandoLibroNoExiste() throws Exception {
        mockMvc.perform(
                get("/api/libros/999")
        ).andExpect(status().isNotFound());
    }
}
