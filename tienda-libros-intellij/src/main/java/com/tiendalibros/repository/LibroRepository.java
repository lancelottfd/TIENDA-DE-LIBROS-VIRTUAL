
package com.tiendalibros.repository;

import com.tiendalibros.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    @Query("SELECT l FROM Libro l WHERE l.stock < :cantidad")
    List<Libro> buscarLibrosConStockBajo(@Param("cantidad") Integer cantidad);

}

