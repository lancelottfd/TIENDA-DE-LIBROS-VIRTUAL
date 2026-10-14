
package com.tiendalibros.service;

import com.tiendalibros.model.Libro;
import com.tiendalibros.model.MovimientoInventario;
import com.tiendalibros.repository.LibroRepository;
import com.tiendalibros.repository.MovimientoInventarioRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class LibroRollbackIntegrationTest {

    @Autowired
    private LibroService libroService;

    @Autowired
    private LibroRepository libroRepository;

    @MockitoSpyBean
    private MovimientoInventarioRepository movimientoRepository;

    @Test
    void debeRevertirStockCuandoFallaMovimiento() {

        // Crear un libro real en MySQL
        Libro libro = new Libro(
                null,
                "Libro prueba rollback",
                "Autor prueba",
                20.0,
                15,
                1L
        );

        libro = libroRepository.saveAndFlush(libro);
        Long libroId = libro.getId();

        // Simular un fallo al registrar el movimiento
        doThrow(new RuntimeException("Fallo simulado"))
                .when(movimientoRepository)
                .save(any(MovimientoInventario.class));

        // Ejecutar el metodo transaccional
        assertThrows(RuntimeException.class,
                () -> libroService.registrarSalida(libroId, 3));

        // Consultar nuevamente el stock desde MySQL
        Libro libroDespues = libroRepository.findById(libroId)
                .orElseThrow();

        // El stock debe mantenerse en 15
        assertEquals(15, libroDespues.getStock());

        // No debe existir un movimiento de salida
        long cantidadMovimientos =
                movimientoRepository.findAll()
                        .stream()
                        .filter(m -> m.getLibro().getId().equals(libroId))
                        .count();

        assertEquals(0, cantidadMovimientos);
    }
}
