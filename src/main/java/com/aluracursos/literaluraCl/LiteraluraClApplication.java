package com.aluracursos.literaluraCl;

import com.aluracursos.literaluraCl.principal.Principal;
import com.aluracursos.literaluraCl.repository.AutorRepository;
import com.aluracursos.literaluraCl.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiteraluraClApplication implements CommandLineRunner {

	@Autowired
	private LibroRepository repositoryLibro;

	@Autowired
	private AutorRepository repositoryAutor;

	public static void main(String[] args) {
		SpringApplication.run(LiteraluraClApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Principal principal = new Principal(repositoryLibro, repositoryAutor);
		principal.muestraElMenu();

	}
}
