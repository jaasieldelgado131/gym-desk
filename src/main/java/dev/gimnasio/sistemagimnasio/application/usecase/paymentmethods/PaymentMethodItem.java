package dev.gimnasio.sistemagimnasio.application.usecase.paymentmethods;

/** {@code cash}: al cobrar se pide lo recibido y se calcula el cambio. */
public record PaymentMethodItem(long id, String name, boolean cash, boolean active) {
}
