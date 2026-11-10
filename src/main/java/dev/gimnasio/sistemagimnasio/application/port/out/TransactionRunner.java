package dev.gimnasio.sistemagimnasio.application.port.out;

import java.util.function.Supplier;

/**
 * Ejecuta un trabajo como una sola unidad: o se guarda todo o nada. Los repositorios que se usan dentro
 * participan en la misma transacción. Si el trabajo choca con otra estación al mismo tiempo, se repite
 * desde el principio; por eso no debe tener efectos fuera de la base (imprimir, enviar).
 */
public interface TransactionRunner {
    <T> T inTransaction(Supplier<T> work);

    default void run(Runnable work) {
        inTransaction(() -> {
            work.run();
            return null;
        });
    }
}
