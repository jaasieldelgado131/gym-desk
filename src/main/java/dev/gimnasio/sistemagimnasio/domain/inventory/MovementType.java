package dev.gimnasio.sistemagimnasio.domain.inventory;

public enum MovementType {
    ENTRADA, // Suma la cantidad a la existencia.
    SALIDA, //Resta la cantidad de la existencia.
    AJUSTE // Fija la existencia en la cantidad contada (p. ej. conteo físico).
}
