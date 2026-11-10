package dev.gimnasio.sistemagimnasio.application.fakes;

import dev.gimnasio.sistemagimnasio.application.port.out.SaleRepository;

public final class InMemorySales implements SaleRepository {
    public int count;

    @Override
    public int count() {
        return count;
    }
}
