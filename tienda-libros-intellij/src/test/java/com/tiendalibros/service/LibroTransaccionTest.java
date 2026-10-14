
package com.tiendalibros.service;

import com.tiendalibros.model.Libro;
import com.tiendalibros.model.MovimientoInventario;
import com.tiendalibros.repository.LibroRepository;
import com.tiendalibros.repository.MovimientoInventarioRepository;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LibroTransaccionTest {

    @Test
    void debePropagarErrorSiFallaRegistroMovimiento() {

        // Crear repositorios simulados
        LibroRepository libroRepository =
                Mockito.mock(LibroRepository.class);

        MovimientoInventarioRepository movimientoRepository =
                Mockito.mock(MovimientoInventarioRepository.class);

        // Crear libro de prueba
        Libro libro = new Libro(
                1L, "El Principito",
                "Antoine de Saint-Exupéry",
                30.0, 15, 1L
        );

        // Simular búsqueda del libro
        when(libroRepository.findById(1L))
                .thenReturn(Optional.of(libro));

        // Simular que el guardado del libro funciona
        when(libroRepository.save(any(Libro.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        // Simular error al registrar movimiento
        when(movimientoRepository.save(
                any(MovimientoInventario.class)))
                .thenThrow(new RuntimeException(
                        "Error al registrar movimiento"));

        LibroService service = new LibroService(
                libroRepository, movimientoRepository);

        // Verificar que la operación falla
        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> service.registrarSalida(1L, 3)
        );

        assertEquals(
                "Error al registrar movimiento",
                error.getMessage()
        );

        verify(libroRepository).save(any(Libro.class));
        verify(movimientoRepository)
                .save(any(MovimientoInventario.class));
    }
}
