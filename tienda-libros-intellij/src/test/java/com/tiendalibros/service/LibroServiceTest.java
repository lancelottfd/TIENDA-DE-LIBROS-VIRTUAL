package com.tiendalibros.service;

import com.tiendalibros.model.Libro;
import com.tiendalibros.repository.LibroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LibroServiceTest {
    private LibroService service;

    @BeforeEach
    void setUp() {
        LibroRepository repository = new LibroRepository();
        service = new LibroService(repository);
    }

    @Test
    void debeCrearUnLibro() {
        Libro libro = new Libro(
                null,
                "El Principito",
                "Antoine de Saint-Exupéry",
                30.0,
                10,
                1L
        );

        Libro resultado = service.crear(libro);

        assertNotNull(resultado.getId());
        assertEquals("El Principito", resultado.getTitulo());
    }

    @Test
    void debeObtenerUnLibroExistente() {
        Libro libro = new Libro(
                null,
                "1984",
                "George Orwell",
                40.0,
                5,
                1L
        );

        Libro creado = service.crear(libro);
        Libro resultado = service.obtenerPorId(creado.getId());

        assertEquals("1984", resultado.getTitulo());
        assertEquals("George Orwell", resultado.getAutor());
    }
}
