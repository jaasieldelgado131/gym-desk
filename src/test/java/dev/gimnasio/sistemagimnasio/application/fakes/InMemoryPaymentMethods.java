package dev.gimnasio.sistemagimnasio.application.fakes;

import dev.gimnasio.sistemagimnasio.application.port.out.PaymentMethodRepository;
import dev.gimnasio.sistemagimnasio.domain.payment.PaymentMethod;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryPaymentMethods implements PaymentMethodRepository {
    private final Map<Long, PaymentMethod> byId = new LinkedHashMap<>();
    private long nextId = 1;
    public int writes;

    public PaymentMethod add(String name, boolean cash, boolean active) {
        PaymentMethod method = new PaymentMethod(nextId++, name, cash, active);
        byId.put(method.id(), method);
        return method;
    }

    public PaymentMethod get(long id) {
        return byId.get(id);
    }

    @Override
    public List<PaymentMethod> findAll(boolean includeInactive) {
        return byId.values().stream()
                .filter(method -> includeInactive || method.active())
                .sorted(Comparator.comparing((PaymentMethod method) -> method.name().toLowerCase()))
                .toList();
    }

    @Override
    public Optional<PaymentMethod> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public boolean existsByName(String name, Long excludeId) {
        return byId.values().stream()
                .anyMatch(method -> method.name().equalsIgnoreCase(name) && !Objects.equals(method.id(), excludeId));
    }

    @Override
    public int countActive() {
        return (int) byId.values().stream().filter(PaymentMethod::active).count();
    }

    @Override
    public PaymentMethod insert(PaymentMethod method) {
        writes++;
        return add(method.name(), method.cash(), method.active());
    }

    @Override
    public void update(PaymentMethod method) {
        writes++;
        byId.put(method.id(), method);
    }
}
