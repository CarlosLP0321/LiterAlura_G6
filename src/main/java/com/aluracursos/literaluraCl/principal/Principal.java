package com.aluracursos.literaluraCl.principal;

import com.aluracursos.literaluraCl.model.*;
import com.aluracursos.literaluraCl.repository.AutorRepository;
import com.aluracursos.literaluraCl.repository.LibroRepository;
import com.aluracursos.literaluraCl.service.ConsumoAPI;
import com.aluracursos.literaluraCl.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private static final String URL_BASE = "https://gutendex.com/books?search=";
    private ConvierteDatos conversor = new ConvierteDatos();
    private LibroRepository repositorioLibro;
    private AutorRepository repositorioAutor;

    public Principal(LibroRepository repositoryLibro, AutorRepository repositoryAutor) {
        this.repositorioLibro = repositoryLibro;
        this.repositorioAutor = repositoryAutor;
    }

    public void muestraElMenu() {
        int opcion = -1;
        while (opcion != 0) {
            var menu = """
                    \n-----------------MENU-----------------
                    1 - Buscar libro por título
                    2 - Listar libros por idioma
                    3 - Listar libros registrados
                    
                    4 - Buscar autor por nombre
                    5 - Listar autores vivos en un determinado año
                    6 - Listar autores registrados
                    
                    7 - Top 10 libros más descargados
                    8 - Estadísticas
                                  
                    0 - Salir
                    
                    Escoja la opción que desee ingresando su número:""";
            System.out.println(menu);

            try {
                opcion = Integer.parseInt(teclado.nextLine());
                System.out.println("--------------------------------------");
            } catch (NumberFormatException e) {
                opcion = -1;
            }

            switch (opcion) {
                case 1:
                    getDatosLibro();
                    break;
                case 2:
                    listarLibrosPorIdioma();
                    break;
                case 3:
                    listarLibrosRegistrados();
                    break;
                case 4:
                    buscarAutorNombre();
                    break;
                case 5:
                    listarAutoresVivosAnio();
                    break;
                case 6:
                    listarAutoresRegistrados();
                    break;
                case 7:
                    top10Libros();
                    break;
                case 8:
                    estadisticas();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
                    System.out.println("--------------------------------------");
            }
        }
    }

    private void mostrarDatosLibro(List<Libro> libros){
        libros.forEach(libro -> {
            Long idLibro = libro.getId();
            List<String> autores = repositorioLibro.autoresPorLibro(idLibro);

            StringBuilder autoresNombres = new StringBuilder();
            for (String nombre : autores) {
                if (!autoresNombres.isEmpty()) {
                    autoresNombres.append("; ");
                }
                autoresNombres.append(nombre);
            }

            System.out.println(
                    "\n-------------LIBRO------------- \nTítulo: " + libro.getTitulo() +
                            "\nAutores: " + autoresNombres.toString() +
                            "\nIdioma: " + libro.getIdiomas() +
                            "\nNumero de descargas: " + libro.getNumeroDescargas() +
                            "\n-------------------------------");
        });
    }

    private void getDatosLibro() {
        System.out.println("\n------------------BUSQUEDA LIBRO------------------");
        System.out.println("Escribe el nombre del libro que deseas buscar:");
        var nombreLibro = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + nombreLibro.replace(" ", "+"));

        Datos datosBusqueda = conversor.obtenerDatos(json, Datos.class);
        Optional<DatosLibro> libroBuscado = datosBusqueda.resultados().stream().findFirst();

        libroBuscado.ifPresentOrElse(
                datosLibro -> {
                    Optional<Libro> libroExistente = repositorioLibro.findByTitulo(datosLibro.titulo());

                    if (libroExistente.isPresent()) {
                        System.out.println("No se puede registrar el mismo libro más de una vez");
                    } else {
                        List<Autor> autoresGuardados = new ArrayList<>();
                        datosLibro.autores().forEach(datosAutor -> {
                            Autor autor = repositorioAutor.findByNombre(datosAutor.nombre())
                                    .orElseGet(() -> {
                                        Autor nuevoAutor = new Autor(datosAutor);
                                        return repositorioAutor.save(nuevoAutor);
                                    });
                            autoresGuardados.add(autor);
                        });

                        Libro nuevoLibro = new Libro(datosLibro);
                        nuevoLibro.setAutores(autoresGuardados);
                        repositorioLibro.save(nuevoLibro);

                        Optional<Libro> optionalLibro = repositorioLibro.findById(nuevoLibro.getId());
                        List<Libro> libros = new ArrayList<>();
                        optionalLibro.ifPresent(libros::add);
                        mostrarDatosLibro(libros);
                    }
                },
                () -> {
                    System.out.println("Libro no encontrado");
                }
        );
        System.out.println("--------------------------------------------------");
    }

    private void listarLibrosRegistrados(){
        System.out.println("\n--------------LIBROS REGISTRADOS--------------");
        List<Libro> libros = repositorioLibro.findAll();

        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados.");
        } else {
            mostrarDatosLibro(libros);
        }
        System.out.println("----------------------------------------------");
    }

    private void listarLibrosPorIdioma(){
        System.out.println("\n--------------LISTADO DE LIBROS POR IDIOMA--------------");
        List<Libro> libros = repositorioLibro.findAll();

        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados");
        } else {
            System.out.println("""
                    es - español
                    en - inglés
                    fr - francés
                    pt - portugués
                    
                    Escriba el idioma para buscar los libros:""");
            var idioma = teclado.nextLine();
            String finalIdioma = idioma.toLowerCase();

            if (idioma.equals("es") || idioma.equals("en") || idioma.equals("fr") || idioma.equals("pt")) {
                List<Libro> librosPorIdioma = libros.stream()
                        .filter(libro -> libro.getIdiomas().contains(finalIdioma))
                        .toList();

                if (librosPorIdioma.isEmpty()){
                    System.out.println("No hay libros registrados con ese idioma");
                } else {
                    mostrarDatosLibro(librosPorIdioma);
                }
            } else {
                System.out.println("Idioma no soportado");
            }
        }
        System.out.println("--------------------------------------------------------");
    }

    private void mostrarDatosAutor(List<Autor> autores){
        autores.forEach(autor -> {
            Long idAutor = autor.getId();
            List<String> libros = repositorioAutor.librosPorAutor(idAutor);

            StringBuilder librosNombres = new StringBuilder();
            for (String titulo : libros) {
                if (!librosNombres.isEmpty()) {
                    librosNombres.append("; ");
                }
                librosNombres.append(titulo);
            }

            System.out.println(
                    "\n------------AUTOR------------ \nNombre: " + autor.getNombre() +
                            "\nFecha de nacimiento: " + autor.getFechaNacimiento() +
                            "\nFecha de fallecimiento: " + autor.getFechaFallecimiento() +
                            "\nLibros: " + librosNombres.toString() +
                            "\n-----------------------------");
        });
    }

    private void buscarAutorNombre(){
        System.out.println("\n------------------BUSQUEDA AUTOR------------------");
        List<Autor> autores = repositorioAutor.findAll();

        if (autores.isEmpty()){
            System.out.println("No hay autores registrados");
        } else {
            System.out.println("Escribe el nombre del autor que deseas buscar:");
            var nombreAutor = teclado.nextLine();

            List<Autor> autoresEncontrados = autores.stream()
                    .filter(autor -> autor.getNombre().toLowerCase().contains(nombreAutor))
                    .collect(Collectors.toList());

            if (autoresEncontrados.isEmpty()) {
                System.out.println("No se encontraron autores por el nombre ingresado");
            } else {
                mostrarDatosAutor(autoresEncontrados);
            }
        }
        System.out.println("--------------------------------------------------");
    }

    private void listarAutoresRegistrados(){
        System.out.println("\n--------------AUTORES REGISTRADOS--------------");
        List<Autor> autores = repositorioAutor.findAll();
        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados");
        } else {
            mostrarDatosAutor(autores);
        }
        System.out.println("-----------------------------------------------");
    }

    private void listarAutoresVivosAnio(){
        int year = 0;
        boolean validInput = false;

        System.out.println("\n------------------BUSQUEDA AUTORES VIVOS------------------");
        List<Autor> autores = repositorioAutor.findAll();

        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados");
        } else {
            System.out.println("Escriba el año del cual desee conocer que autores se encontraban vivos:");
            try {
                year = Integer.parseInt(teclado.nextLine());
                validInput = true;
            } catch (NumberFormatException e) {
                System.out.println("Entrada no válida. Por favor, ingrese un número.");
            }

            if (validInput) {
                int finalYear = year;
                List<Autor> autoresVivos = autores.stream()
                        .filter(autor -> Integer.parseInt(autor.getFechaNacimiento()) <= finalYear && (autor.getFechaFallecimiento() == null || Integer.parseInt(autor.getFechaFallecimiento()) > finalYear))
                        .sorted(Comparator.comparing(Autor::getNombre))
                        .toList();

                if (autoresVivos.isEmpty()) {
                    System.out.println("No hay autores vivos en ese año");
                } else {
                    mostrarDatosAutor(autoresVivos);
                }
            }
        }
        System.out.println("----------------------------------------------------------");
    }

    private void top10Libros(){
        var json = consumoAPI.obtenerDatos(URL_BASE);
        var datos = conversor.obtenerDatos(json, Datos.class);

        System.out.println("\n------------TOP 10 LIBROS MÁS DESCARGADOS------------");
        datos.resultados().stream()
                .sorted(Comparator.comparing(DatosLibro::numeroDescargas).reversed())
                .limit(10)
                .map(DatosLibro::titulo)
                .forEach(System.out::println);
        System.out.println("-----------------------------------------------------");
    }

    private void estadisticas(){
        var json = consumoAPI.obtenerDatos(URL_BASE);
        var datos = conversor.obtenerDatos(json, Datos.class);

        DoubleSummaryStatistics est = datos.resultados().stream()
                .filter(d -> d.numeroDescargas() >0 )
                .collect(Collectors.summarizingDouble(DatosLibro::numeroDescargas));

        System.out.println("\n------------ESTADÍSTICAS------------");
        System.out.println("Cantidad media de descargas: " + est.getAverage());
        System.out.println("Cantidad máxima de descargas: "+ est.getMax());
        System.out.println("Cantidad mínima de descargas: " + est.getMin());
        System.out.println("Cantidad de registros evaluados para calcular las estadisticas: " + est.getCount());
        System.out.println("------------------------------------");
    }
}
