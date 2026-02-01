package com.relatosdepapel.ms_books_catalogue.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.relatosdepapel.ms_books_catalogue.dto.AvailabilityResponseDTO;
import com.relatosdepapel.ms_books_catalogue.dto.BookPatchDTO;
import com.relatosdepapel.ms_books_catalogue.dto.BookRequestDTO;
import com.relatosdepapel.ms_books_catalogue.dto.BookResponseDTO;
import com.relatosdepapel.ms_books_catalogue.dto.StockUpdateDTO;
import com.relatosdepapel.ms_books_catalogue.entity.Book;
import com.relatosdepapel.ms_books_catalogue.repository.BookRepository;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de libros.
 * Contiene la lógica de negocio para la gestión del catálogo.
 */
@Service // Indica que es un servicio
@RequiredArgsConstructor // Inyección de dependencias sin constructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository; // Dependencia del repositorio

    // METODOS CRUD

    /**
     * Obtiene todos los libros del catálogo.
     * 
     * @return Lista de todos los libros como BookResponseDTO
     */
    @Override
    public List<BookResponseDTO> getAll() {
        // convertir la lista de entidades a DTOs
        return bookRepository.getAll().stream().map(this::toResponseDTO).toList();
    }

    /**
     * Busca un libro por ID.
     * 
     * @param id ID del libro a buscar
     * @return BookResponseDTO si existe, null si no existe
     */
    @Override
    public BookResponseDTO getById(Long id) {
        Book book = bookRepository.getById(id); // buscar el libro por ID
        if (book == null) {
            return null; // si no existe, devolver null
        }
        return toResponseDTO(book); // convertir la entidad a DTO y devolverlo
    }

    /**
     * Crea un nuevo libro en el catálogo.
     * 
     * @param dto Datos del libro a crear
     * @return BookResponseDTO del libro creado (con ID generado)
     * @throws IllegalArgumentException si el ISBN ya existe
     */
    @Override
    public BookResponseDTO create(BookRequestDTO dto) {
        // validar que el ISBN no exista
        if (bookRepository.existsByIsbn(dto.getIsbn())) {
            throw new IllegalArgumentException("El ISBN ya existe: " + dto.getIsbn());
        }
        // convertir el DTO a entidad
        Book book = toEntity(dto);
        // guardar el libro
        Book savedBook = bookRepository.save(book);
        // convertir la entidad a DTO y devolverlo
        return toResponseDTO(savedBook);
    }

    /**
     * Actualiza un libro existente (PUT - actualización completa).
     * Actualiza todos los campos excepto ID e ISBN.
     * 
     * @param id  ID del libro a actualizar
     * @param dto Nuevos datos del libro
     * @return BookResponseDTO actualizado o null si no existe
     */
    @Override
    public BookResponseDTO update(Long id, BookRequestDTO dto) {
        Book book = bookRepository.getById(id); // buscar el libro por ID
        if (book == null) {
            return null; // si no existe, devolver null
        }
        book.updateFromDTO(dto); // actualizar el libro con los datos del DTO
        Book updatedBook = bookRepository.save(book); // guardar el libro actualizado
        return toResponseDTO(updatedBook); // convertir la entidad a DTO y devolverlo
    }

    /**
     * Actualiza parcialmente un libro existente (PATCH - actualización selectiva).
     * Solo actualiza los campos proporcionados en el DTO.
     * 
     * @param id  ID del libro a actualizar
     * @param dto Campos a actualizar (solo los proporcionados)
     * @return BookResponseDTO actualizado o null si no existe
     */
    @Override
    public BookResponseDTO patch(Long id, BookPatchDTO dto) {
        // busca libro por ID
        Book book = bookRepository.getById(id);
        // si no existe retornar null
        if (book == null) {
            return null;
        }
        // actualizar campos opcionales
        if (dto.getTitle() != null) {
            book.setTitle(dto.getTitle());
        }
        if (dto.getAuthor() != null) {
            book.setAuthor(dto.getAuthor());
        }
        if (dto.getPublicationDate() != null) {
            book.setPublicationDate(dto.getPublicationDate());
        }
        if (dto.getCategory() != null) {
            book.setCategory(dto.getCategory());
        }
        if (dto.getRating() != null) {
            book.setRating(dto.getRating());
        }
        if (dto.getVisible() != null) {
            book.setVisible(dto.getVisible());
        }
        if (dto.getStock() != null) {
            book.setStock(dto.getStock());
        }
        if (dto.getPrice() != null) {
            book.setPrice(dto.getPrice());
        }
        // guardar el libro actualizado
        Book patchedBook = bookRepository.save(book);
        // convertir la entidad a DTO y devolverlo
        return toResponseDTO(patchedBook);
    }

    /**
     * Elimina un libro del catálogo.
     * 
     * @param id ID del libro a eliminar
     * @return true si se eliminó, false si no existía
     */
    @Override
    public boolean delete(Long id) {
        // busca libro por ID
        Book book = bookRepository.getById(id);
        // si no existe retornar false
        if (book == null) {
            return false;
        }
        // eliminar el libro
        bookRepository.delete(book);
        // retornar true
        return true;
    }

    // METODOS DE BUSQUEDA DINAMICA

    @Override
    public List<BookResponseDTO> search(String title, String author, String category, String isbn, Integer ratingMin,
            Integer ratingMax, Boolean visible, BigDecimal minPrice, BigDecimal maxPrice, LocalDate publicationDateFrom,
            LocalDate publicationDateTo, Integer minStock) {
        // TODO
        return null;
    }

    // METODOS ESPECIALES

    @Override
    public AvailabilityResponseDTO checkAvailability(Long id) {
        // TODO
        return null;
    }

    @Override
    public BookResponseDTO updateStock(Long id, StockUpdateDTO dto) {
        // TODO
        return null;
    }

    // METODOS HELPERS

    /**
     * Convierte una entidad Book a BookResponseDTO.
     * 
     * @param book Entidad Book de la base de datos
     * @return BookResponseDTO para enviar al cliente
     */
    private BookResponseDTO toResponseDTO(Book book) {
        return new BookResponseDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublicationDate(),
                book.getCategory(),
                book.getIsbn(),
                book.getRating(),
                book.getVisible(),
                book.getStock(),
                book.getPrice());
    }

    /**
     * Convierte un BookRequestDTO a entidad Book.
     * Nota: El ID se genera automáticamente por JPA, no se incluye.
     * 
     * @param dto DTO recibido del cliente
     * @return Entidad Book lista para guardar en BD
     */
    private Book toEntity(BookRequestDTO dto) {
        return Book.builder()
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .publicationDate(dto.getPublicationDate())
                .category(dto.getCategory())
                .isbn(dto.getIsbn())
                .rating(dto.getRating())
                .visible(dto.getVisible())
                .stock(dto.getStock())
                .price(dto.getPrice())
                .build();
    }

}
