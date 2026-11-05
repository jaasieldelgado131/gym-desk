package dev.gimnasio.sistemagimnasio.domain.payment;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CashPaymentTest {

    @Test
    void elCambio_esLoRecibidoMenosElTotal() {
        assertEquals(Money.of("50.00"), new CashPayment(Money.of("450"), Money.of("500")).change());
        assertEquals(Money.of("0.01"), new CashPayment(Money.of("99.99"), Money.of("100")).change());
    }

    @Test
    void pagoExacto_noDaCambio() {
        assertEquals(Money.ZERO, new CashPayment(Money.of("450"), Money.of("450.00")).change());
    }

    @Test
    void recibirUnCentavoMenosQueElTotal_seRechaza() {
        assertThrows(DomainException.class, () -> new CashPayment(Money.of("450"), Money.of("449.99")));
    }

    @Test
    void totalORecibidoAusentes_seRechazan() {
        assertThrows(DomainException.class, () -> new CashPayment(null, Money.of("500")));
        assertThrows(DomainException.class, () -> new CashPayment(Money.of("450"), null));
    }
}
