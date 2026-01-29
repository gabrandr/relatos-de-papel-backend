package com.relatosdepapel.ms_books_catalogue.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.relatosdepapel.ms_books_catalogue.utils.Consts;
import com.relatosdepapel.ms_books_catalogue.dto.BookRequestDTO;

/**
 * Entidad que representa un libro
 */
@Entity // Indica que es una entidad
@Table(name = "books") // Indica el nombre de la tabla
@Data // Genera los getters y setters
@NoArgsConstructor // Genera el constructor vacio
@AllArgsConstructor // Genera el constructor con todos los atributos
@Builder // Patron Builder
public class Book {
    @Id // Marca clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Genera ID autoincrementable
    private Long id;

    @Column(name = Consts.TITLE, nullable = false)
    private String title;

    @Column(name = Consts.AUTHOR, nullable = false)
    private String author;

    @Column(name = Consts.PUBLICATION_DATE)
    private LocalDate publicationDate;

    @Column(name = Consts.CATEGORY)
    private String category;

    @Column(name = Consts.ISBN, unique = true, nullable = false)
    private String isbn;

    @Column(name = Consts.RATING)
    private Integer rating;

    @Column(name = Consts.VISIBLE, nullable = false)
    private Boolean visible;

    @Column(name = Consts.STOCK, nullable = false)
    private Integer stock;

    @Column(name = Consts.PRICE, nullable = false)
    private BigDecimal price;

    // Metodo para actualizar Book desde un BookRequestDTO
    public void updateFromDTO(BookRequestDTO dto) {
        this.title = dto.getTitle();
        this.author = dto.getAuthor();
        this.publicationDate = dto.getPublicationDate();
        this.category = dto.getCategory();
        this.rating = dto.getRating();
        this.visible = dto.getVisible();
        this.stock = dto.getStock();
        this.price = dto.getPrice();
    }
}
