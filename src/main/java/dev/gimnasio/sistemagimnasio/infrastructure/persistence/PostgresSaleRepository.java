package dev.gimnasio.sistemagimnasio.infrastructure.persistence;

import dev.gimnasio.sistemagimnasio.application.port.out.SaleRepository;
import org.jdbi.v3.core.Jdbi;

import java.util.Objects;

public final class PostgresSaleRepository implements SaleRepository {
    private final Jdbi jdbi;

    public PostgresSaleRepository(Jdbi jdbi) {
        this.jdbi = Objects.requireNonNull(jdbi, "jdbi");
    }

    @Override
    public int count() {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT COUNT(*)
                        FROM venta
                        """)
                .mapTo(Integer.class)
                .one());
    }
}
