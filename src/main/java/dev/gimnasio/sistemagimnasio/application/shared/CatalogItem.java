package dev.gimnasio.sistemagimnasio.application.shared;

/**
 * Un elemento de catálogo. {@code cash} solo aplica a métodos de pago y {@code system} solo a motivos de
 * movimiento; en los demás catálogos son {@code false}.
 */
public record CatalogItem(long id, String name, boolean active, boolean cash, boolean system) {
}
