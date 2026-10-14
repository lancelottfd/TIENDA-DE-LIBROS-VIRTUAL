
package com.tiendalibros.service;

import com.tiendalibros.model.Libro;
import com.tiendalibros.repository.LibroRepository;
import com.tiendalibros.repository.MovimientoInventarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LibroServiceTest {

    private LibroService service;
    private LibroRepository libroRepository;
    private MovimientoInventarioRepository movimientoRepository;

    @BeforeEach
    void setUp() {
        libroRepository = mock(LibroRepository.class);
        movimientoRepository = mock(MovimientoInventarioRepository.class);

        service = new LibroService(
                libroRepository,
                movimientoRepository
        );
    }

    @Test
    void debeCrearUnLibro() {

        Libro libro = new Libro(
                null, "El Principito",
                "Antoine de Saint-Exupéry",
                30.0, 10, 1L
        );

        Libro guardado = new Libro(
                1L, "El Principito",
                "Antoine de Saint-Exupéry",
                30.0, 10, 1L
        );

        when(libroRepository.save(any(Libro.class)))
                .thenReturn(guardado);

        Libro resultado = service.crear(libro);

        assertNotNull(resultado.getId());
        assertEquals("El Principito", resultado.getTitulo());

        verify(libroRepository).save(any(Libro.class));
    }

    @Test
    void debeObtenerUnLibroExistente() {

        Libro libro = new Libro(
                1L, "1984", "George Orwell",
                30.0, 5, 1L
        );

        when(libroRepository.findById(1L))
                .thenReturn(Optional.of(libro));

        Libro resultado = service.obtenerPorId(1L);

        assertEquals("1984", resultado.getTitulo());
        assertEquals("George Orwell", resultado.getAutor());
    }
}

