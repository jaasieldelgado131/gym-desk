package dev.gimnasio.sistemagimnasio.infrastructure.persistence;

import dev.gimnasio.sistemagimnasio.domain.payment.PaymentMethod;
import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers(disabledWithoutDocker = true)
class JdbiTransactionRunnerTest {
    private Jdbi jdbi;
    private JdbiTransactionRunner transactions;
    private PostgresPaymentMethodRepository methods;

    @BeforeEach
    void setUp() {
        jdbi = TestDatabase.jdbi();
        TestDatabase.clean();
        transactions = new JdbiTransactionRunner(jdbi);
        methods = new PostgresPaymentMethodRepository(jdbi);
    }

    private PaymentMethod insert(String name) {
        return methods.insert(new PaymentMethod(null, name, false, true));
    }

    @Test
    void siElTrabajoTermina_seGuardaTodo() {
        String result = transactions.inTransaction(() -> {
            insert("Efectivo");
            insert("Tarjeta");
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(2, methods.findAll(true).size());
    }

    @Test
    void siElTrabajoFalla_seDeshaceLoQueHicieronLosRepositorios() {
        DomainException failure = new DomainException("regla rota");

        DomainException thrown = assertThrows(DomainException.class, () -> transactions.run(() -> {
            insert("Efectivo");
            throw failure;
        }));

        assertSame(failure, thrown);
        assertEquals(List.of(), methods.findAll(true));
    }

    @Test
    void unChoqueConOtraEstacion_repiteElTrabajo() {
        AtomicInteger attempts = new AtomicInteger();

        String result = transactions.inTransaction(() -> {
            if (attempts.incrementAndGet() == 1) {
                insert("Efectivo");
                throw serializationFailure();
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(2, attempts.get());
        assertEquals(List.of(), methods.findAll(true));
    }

    @Test
    void siSigueChocando_seRindeTrasElMaximoDeIntentos() {
        AtomicInteger attempts = new AtomicInteger();

        assertThrows(RuntimeException.class, () -> transactions.run(() -> {
            attempts.incrementAndGet();
            throw serializationFailure();
        }));

        assertEquals(JdbiTransactionRunner.MAX_ATTEMPTS, attempts.get());
    }

    @Test
    void otrosErrores_noSeRepiten() {
        AtomicInteger attempts = new AtomicInteger();

        assertThrows(DomainException.class, () -> transactions.run(() -> {
            attempts.incrementAndGet();
            throw new DomainException("regla rota");
        }));

        assertEquals(1, attempts.get());
    }

    /** Dos estaciones desactivan a la vez los dos únicos métodos activos: al final queda uno. */
    @Test
    void dosEstacionesAlMismoTiempo_noDejanElCatalogoSinMetodosActivos() throws Exception {
        PaymentMethod efectivo = insert("Efectivo");
        PaymentMethod tarjeta = insert("Tarjeta");
        CountDownLatch bothCounted = new CountDownLatch(2);
        ExecutorService stations = Executors.newFixedThreadPool(2);
        try {
            List<Future<Boolean>> results = stations.invokeAll(List.of(
                    () -> deactivateIfNotLast(efectivo, bothCounted),
                    () -> deactivateIfNotLast(tarjeta, bothCounted)), 30, TimeUnit.SECONDS);
            for (Future<Boolean> result : results) {
                result.get();
            }
        } finally {
            stations.shutdownNow();
        }

        assertEquals(1, methods.countActive());
    }

    private boolean deactivateIfNotLast(PaymentMethod method, CountDownLatch bothCounted) {
        return transactions.inTransaction(() -> {
            int active = methods.countActive();
            bothCounted.countDown();
            await(bothCounted);
            if (active <= 1) {
                return false;
            }
            methods.update(new PaymentMethod(method.id(), method.name(), method.cash(), false));
            return true;
        });
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static RuntimeException serializationFailure() {
        return new IllegalStateException(new PSQLException("could not serialize access", PSQLState.SERIALIZATION_FAILURE));
    }
}
