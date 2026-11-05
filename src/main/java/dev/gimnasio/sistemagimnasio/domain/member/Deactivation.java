package dev.gimnasio.sistemagimnasio.domain.member;

import java.time.LocalDate;

/** Baja de un socio: fecha (su último vencimiento) y, si volvió, fecha de la reactivación (null si sigue de baja). */
public record Deactivation(LocalDate date, LocalDate reactivatedOn) {
}
