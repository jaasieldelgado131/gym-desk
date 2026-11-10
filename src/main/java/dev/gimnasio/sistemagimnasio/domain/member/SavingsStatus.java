package dev.gimnasio.sistemagimnasio.domain.member;

public enum SavingsStatus {
    /** Dura 30 días o menos: no hay contra qué comparar. */
    NO_APLICA,
    /** Falta una referencia mensual válida o la duración no equivale a meses completos. */
    NO_DISPONIBLE,
    SIN_AHORRO,
    AHORRO
}
