package com.relatosdepapel.ms_books_catalogue.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para actualizar el stock de un libro
 * Usado por ms-books-payments al procesar una compra
 * La cantidad puede ser positiva (aumentar) o negativa (decrementar)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateDTO {
    private Integer quantity; // Ejemplo: -2 para decrementar 2 unidades
}
