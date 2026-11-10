package dev.gimnasio.sistemagimnasio.application.usecase.plans;

import dev.gimnasio.sistemagimnasio.application.port.out.PlanRepository;
import dev.gimnasio.sistemagimnasio.application.shared.NotFoundException;
import dev.gimnasio.sistemagimnasio.application.shared.ValidationException;
import dev.gimnasio.sistemagimnasio.domain.member.Plan;
import dev.gimnasio.sistemagimnasio.domain.member.PlanSavings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Lo que comparten los casos de uso de planes. */
final class PlanRules {
    static final String NAME_REQUIRED = "Escribe el nombre del plan.";
    static final String NAME_TOO_LONG = "Usa " + Plan.MAX_NAME_LENGTH + " caracteres o menos.";
    static final String NAME_TAKEN = "Ya existe un plan con ese nombre.";
    static final String DURATION_RANGE = "Escribe la duración en días, de " + Plan.MIN_DURATION_DAYS + " a "
            + Plan.MAX_DURATION_DAYS + ".";
    static final String PRICE_REQUIRED = "Escribe un precio mayor que cero, por ejemplo 450.00.";
    static final String REFERENCE_INVALID = "Elige un plan de " + Plan.MONTHLY_DAYS + " días distinto de este.";
    static final String NOT_FOUND = "El plan ya no existe. Recarga la pantalla.";

    private PlanRules() {
    }

    /**
     * Valida todos los campos y lanza un solo {@link ValidationException} con los errores.
     *
     * @param currentId el propio plan al editarlo; null al crear
     * @return el plan listo para guardar, con el nombre recortado
     */
    static Plan validPlan(PlanCommand command, Long currentId, boolean active, PlanRepository plans) {
        String name = command.name() == null ? "" : command.name().strip();
        Map<String, String> errors = new LinkedHashMap<>();
        if (name.isEmpty()) {
            errors.put("name", NAME_REQUIRED);
        } else if (!Plan.isValidName(name)) {
            errors.put("name", NAME_TOO_LONG);
        } else if (plans.existsByName(name, currentId)) {
            errors.put("name", NAME_TAKEN);
        }
        Integer days = command.durationDays();
        if (!Plan.isValidDuration(days)) {
            errors.put("durationDays", DURATION_RANGE);
        }
        if (!Plan.isValidPrice(command.price())) {
            errors.put("price", PRICE_REQUIRED);
        }
        Long referenceId = command.referenceMonthlyPlanId();
        if (referenceId != null && !isValidReference(referenceId, currentId, plans)) {
            errors.put("referenceMonthlyPlanId", REFERENCE_INVALID);
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        return new Plan(currentId, name, days, command.price(), referenceId, active);
    }

    static Plan find(PlanRepository plans, long id) {
        return plans.findById(id).orElseThrow(() -> new NotFoundException(NOT_FOUND));
    }

    static Map<Long, Plan> byId(PlanRepository plans) {
        return plans.findAll().stream().collect(Collectors.toMap(Plan::id, Function.identity()));
    }

    static PlanRow toRow(Plan plan, Map<Long, Plan> byId) {
        Plan reference = plan.referenceMonthlyPlanId() == null ? null : byId.get(plan.referenceMonthlyPlanId());
        return new PlanRow(
                plan.id(),
                plan.name(),
                plan.durationDays(),
                plan.price(),
                plan.active(),
                plan.referenceMonthlyPlanId(),
                reference == null ? null : reference.name(),
                SavingsView.of(PlanSavings.of(plan.durationDays(), plan.price(), reference)));
    }

    /** La fila de un plan recién guardado, con su referencia leída de nuevo. */
    static PlanRow toRow(Plan plan, PlanRepository plans) {
        Map<Long, Plan> byId = new LinkedHashMap<>();
        if (plan.referenceMonthlyPlanId() != null) {
            plans.findById(plan.referenceMonthlyPlanId()).ifPresent(reference -> byId.put(reference.id(), reference));
        }
        return toRow(plan, byId);
    }

    private static boolean isValidReference(long referenceId, Long currentId, PlanRepository plans) {
        if (Objects.equals(referenceId, currentId)) {
            return false;
        }
        return plans.findById(referenceId).map(reference -> reference.durationDays() == Plan.MONTHLY_DAYS).orElse(false);
    }
}
