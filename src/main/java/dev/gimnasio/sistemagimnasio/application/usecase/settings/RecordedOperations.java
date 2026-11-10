package dev.gimnasio.sistemagimnasio.application.usecase.settings;

import dev.gimnasio.sistemagimnasio.application.port.out.MembershipPaymentRepository;
import dev.gimnasio.sistemagimnasio.application.port.out.SaleRepository;

/** Cobros y ventas ya registrados; con alguno, la moneda ya no se puede cambiar. */
record RecordedOperations(int payments, int sales) {

    static RecordedOperations count(MembershipPaymentRepository payments, SaleRepository sales) {
        return new RecordedOperations(payments.count(), sales.count());
    }

    boolean locksCurrency() {
        return payments + sales > 0;
    }
}
