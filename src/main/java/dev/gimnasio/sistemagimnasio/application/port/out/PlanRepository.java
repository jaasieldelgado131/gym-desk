package dev.gimnasio.sistemagimnasio.application.port.out;

import dev.gimnasio.sistemagimnasio.domain.member.Plan;

import java.util.List;
import java.util.Optional;

public interface PlanRepository {
    /** Activos e inactivos, sin orden garantizado. */
    List<Plan> findAll();

    Optional<Plan> findById(long id);

    /** Sin distinguir mayúsculas; {@code excludeId} (puede ser null) no cuenta. */
    boolean existsByName(String name, Long excludeId);

    /** @return el mismo plan con el id asignado */
    Plan insert(Plan plan);

    /** @throws IllegalStateException si el id no existe */
    void update(Plan plan);

    boolean hasMemberships(long id);

    /** Nombres de los planes que lo usan como referencia mensual, en orden alfabético. */
    List<String> namesReferencing(long id);

    void delete(long id);
}
