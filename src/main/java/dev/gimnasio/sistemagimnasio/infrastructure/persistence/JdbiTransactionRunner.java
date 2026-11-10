package dev.gimnasio.sistemagimnasio.infrastructure.persistence;

import dev.gimnasio.sistemagimnasio.application.port.out.TransactionRunner;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;

import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Transacción {@code SERIALIZABLE}: PostgreSQL aborta una de dos operaciones que se pisan (por ejemplo, dos
 * estaciones desactivando los dos últimos métodos de pago) y aquí se repite el trabajo completo.
 * Los repositorios llaman a {@code jdbi.withHandle} y JDBI les da el mismo handle mientras dure el trabajo.
 */
public final class JdbiTransactionRunner implements TransactionRunner {
    static final int MAX_ATTEMPTS = 3;
    private static final String SERIALIZATION_FAILURE = "40001";
    private static final String DEADLOCK_DETECTED = "40P01";

    private final Jdbi jdbi;

    public JdbiTransactionRunner(Jdbi jdbi) {
        this.jdbi = Objects.requireNonNull(jdbi, "jdbi");
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        RuntimeException lastException = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return jdbi.inTransaction(TransactionIsolationLevel.SERIALIZABLE, handle -> work.get());
            } catch (RuntimeException e) {
                if (!isRetryable(e)) {
                    throw e;
                }
                lastException = e;
            }
        }
        throw new IllegalStateException("Se agotaron los " + MAX_ATTEMPTS
                + " intentos de la transacción por conflictos con otra estación.", lastException);
    }

    private static boolean isRetryable(Throwable e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql) {
                String state = sql.getSQLState();
                if (SERIALIZATION_FAILURE.equals(state) || DEADLOCK_DETECTED.equals(state)) {
                    return true;
                }
            }
        }
        return false;
    }
}
