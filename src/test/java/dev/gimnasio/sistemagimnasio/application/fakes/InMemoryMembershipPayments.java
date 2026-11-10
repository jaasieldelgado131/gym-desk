package dev.gimnasio.sistemagimnasio.application.fakes;

import dev.gimnasio.sistemagimnasio.application.port.out.MembershipPaymentRepository;

public final class InMemoryMembershipPayments implements MembershipPaymentRepository {
    public int count;

    @Override
    public int count() {
        return count;
    }
}
