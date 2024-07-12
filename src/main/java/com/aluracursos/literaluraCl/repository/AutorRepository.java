package com.aluracursos.literaluraCl.repository;

import com.aluracursos.literaluraCl.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    Optional<Autor> findByNombre(String nombre);

    @Query(value = "SELECT l.titulo FROM Libros l JOIN libro_autor la ON l.id = la.libro_id JOIN Autores a ON la.autor_id = a.id WHERE a.id = :idAutor", nativeQuery = true)
    List<String> librosPorAutor(@Param("idAutor") Long idAutor);
}
