package dev.gimnasio.sistemagimnasio.domain.member;

import java.time.LocalDate;

/** {@code date} es el último vencimiento; {@code reactivatedOn} es null mientras siga de baja. */
public record Deactivation(LocalDate date, LocalDate reactivatedOn) {
}
