package com.aluracursos.literaluraCl.repository;

import com.aluracursos.literaluraCl.model.Autor;
import com.aluracursos.literaluraCl.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    Optional<Libro> findByTitulo(String nombre);

    @Query(value = "SELECT a.nombre FROM Autores a JOIN libro_autor la ON a.id = la.autor_id JOIN Libros l ON la.libro_id = l.id WHERE l.id = :idLibro", nativeQuery = true)
    List<String> autoresPorLibro(@Param("idLibro") Long idLibro);
}
