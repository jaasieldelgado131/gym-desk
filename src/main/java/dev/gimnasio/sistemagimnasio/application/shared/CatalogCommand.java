package dev.gimnasio.sistemagimnasio.application.shared;

/** {@code cash} solo cuenta en métodos de pago; en los demás catálogos se ignora. */
public record CatalogCommand(String name, boolean cash) {
}
