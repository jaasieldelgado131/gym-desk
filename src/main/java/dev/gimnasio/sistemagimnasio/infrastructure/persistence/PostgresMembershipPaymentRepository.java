package dev.gimnasio.sistemagimnasio.infrastructure.persistence;

import dev.gimnasio.sistemagimnasio.application.port.out.MembershipPaymentRepository;
import org.jdbi.v3.core.Jdbi;

import java.util.Objects;

public final class PostgresMembershipPaymentRepository implements MembershipPaymentRepository {
    private final Jdbi jdbi;

    public PostgresMembershipPaymentRepository(Jdbi jdbi) {
        this.jdbi = Objects.requireNonNull(jdbi, "jdbi");
    }

    @Override
    public int count() {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT COUNT(*)
                        FROM pago
                        """)
                .mapTo(Integer.class)
                .one());
    }
}
