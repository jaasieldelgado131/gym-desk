package dev.gimnasio.sistemagimnasio.domain.inventory;

public enum MovementType {
    ENTRADA,
    SALIDA, //Resta la cantidad de la existencia.
    AJUSTE // fija la existencia en la cantidad contada, no la suma
}
