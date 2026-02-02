package com.relatosdepapel.ms_books_catalogue.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para actualización parcial (PATCH)
 * Todos los campos son opcionales
 * No incluye ID (viene en la URL) ni ISBN (no se debe modificar)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookPatchDTO {
    private String title;
    private String author;
    private LocalDate publicationDate;
    private String category;
    private Integer rating;
    private Boolean visible;
    private Integer stock;
    private BigDecimal price;
}
