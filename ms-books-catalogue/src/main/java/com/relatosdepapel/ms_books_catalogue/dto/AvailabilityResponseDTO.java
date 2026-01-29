package com.relatosdepapel.ms_books_catalogue.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;

/**
 * DTO para verificar disponibilidad de un libro
 * Usado por ms-books-payments antes de procesar un pago
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponseDTO {
    private Long id;
    private String title;
    private String isbn;
    private Boolean available; // true si visible=true Y stock>0
    private Boolean visible;
    private Integer stock;
    private BigDecimal price;
}
