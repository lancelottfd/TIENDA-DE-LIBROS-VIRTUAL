
package com.tiendalibros.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiendalibros.exception.GlobalExceptionHandler;
import com.tiendalibros.model.Libro;
import com.tiendalibros.repository.LibroRepository;
import com.tiendalibros.repository.MovimientoInventarioRepository;
import com.tiendalibros.service.LibroService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LibroControllerTest {

    private MockMvc mockMvc;
    private LibroRepository libroRepository;

    @BeforeEach
    void setUp() {

        libroRepository = mock(LibroRepository.class);

        MovimientoInventarioRepository movimientoRepository =
                mock(MovimientoInventarioRepository.class);

        LibroService service = new LibroService(
                libroRepository, movimientoRepository
        );

        LibroController controller = new LibroController(service);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void debeCrearLibroCon201() throws Exception {

        Libro libro = new Libro(
                1L, "Cien años de soledad",
                "Gabriel García Márquez",
                45.0, 10, 1L
        );

        when(libroRepository.save(any(Libro.class)))
                .thenReturn(libro);

        mockMvc.perform(post("/api/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(libro)))
                .andExpect(status().isCreated());
    }

    @Test
    void debeRetornar404CuandoLibroNoExiste() throws Exception {

        when(libroRepository.findById(999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/libros/999"))
                .andExpect(status().isNotFound());
    }
}
