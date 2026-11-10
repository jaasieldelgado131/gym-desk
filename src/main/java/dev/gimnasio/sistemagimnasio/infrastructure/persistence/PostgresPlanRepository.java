package dev.gimnasio.sistemagimnasio.infrastructure.persistence;

import dev.gimnasio.sistemagimnasio.application.port.out.PlanRepository;
import dev.gimnasio.sistemagimnasio.domain.member.Plan;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class PostgresPlanRepository implements PlanRepository {
    private final Jdbi jdbi;

    public PostgresPlanRepository(Jdbi jdbi) {
        this.jdbi = Objects.requireNonNull(jdbi, "jdbi");
    }

    @Override
    public List<Plan> findAll() {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT id, nombre, duracion_dias, precio, referencia_mensual_id, activo
                        FROM plan_membresia
                        ORDER BY id
                        """)
                .map(PostgresPlanRepository::map)
                .list());
    }

    @Override
    public Optional<Plan> findById(long id) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT id, nombre, duracion_dias, precio, referencia_mensual_id, activo
                        FROM plan_membresia
                        WHERE id = :id
                        """)
                .bind("id", id)
                .map(PostgresPlanRepository::map)
                .findOne());
    }

    @Override
    public boolean existsByName(String name, Long excludeId) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT EXISTS (
                            SELECT 1
                            FROM plan_membresia
                            WHERE LOWER(nombre) = LOWER(:nombre)
                              AND (CAST(:excludeId AS BIGINT) IS NULL OR id <> CAST(:excludeId AS BIGINT))
                        )
                        """)
                .bind("nombre", name)
                .bind("excludeId", excludeId)
                .mapTo(Boolean.class)
                .one());
    }

    @Override
    public Plan insert(Plan plan) {
        long id = jdbi.withHandle(handle -> handle.createUpdate("""
                        INSERT INTO plan_membresia (nombre, duracion_dias, precio, referencia_mensual_id, activo)
                        VALUES (:nombre, :duracion, :precio, :referencia, :activo)
                        """)
                .bind("nombre", plan.name())
                .bind("duracion", plan.durationDays())
                .bind("precio", plan.price().amount())
                .bind("referencia", plan.referenceMonthlyPlanId())
                .bind("activo", plan.active())
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .one());
        return new Plan(id, plan.name(), plan.durationDays(), plan.price(), plan.referenceMonthlyPlanId(), plan.active());
    }

    @Override
    public void update(Plan plan) {
        int updated = jdbi.withHandle(handle -> handle.createUpdate("""
                        UPDATE plan_membresia
                        SET nombre                = :nombre,
                            duracion_dias         = :duracion,
                            precio                = :precio,
                            referencia_mensual_id = :referencia,
                            activo                = :activo
                        WHERE id = :id
                        """)
                .bind("id", plan.id())
                .bind("nombre", plan.name())
                .bind("duracion", plan.durationDays())
                .bind("precio", plan.price().amount())
                .bind("referencia", plan.referenceMonthlyPlanId())
                .bind("activo", plan.active())
                .execute());
        if (updated != 1) {
            throw new IllegalStateException("No existe el plan " + plan.id() + ".");
        }
    }

    @Override
    public boolean hasMemberships(long id) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT EXISTS (
                            SELECT 1
                            FROM membresia
                            WHERE plan_id = :id
                        )
                        """)
                .bind("id", id)
                .mapTo(Boolean.class)
                .one());
    }

    @Override
    public List<String> namesReferencing(long id) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT nombre
                        FROM plan_membresia
                        WHERE referencia_mensual_id = :id
                        ORDER BY LOWER(nombre)
                        """)
                .bind("id", id)
                .mapTo(String.class)
                .list());
    }

    @Override
    public void delete(long id) {
        jdbi.useHandle(handle -> handle.createUpdate("""
                        DELETE FROM plan_membresia
                        WHERE id = :id
                        """)
                .bind("id", id)
                .execute());
    }

    private static Plan map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Plan(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getInt("duracion_dias"),
                new Money(rs.getBigDecimal("precio")),
                rs.getObject("referencia_mensual_id", Long.class),
                rs.getBoolean("activo"));
    }
}
