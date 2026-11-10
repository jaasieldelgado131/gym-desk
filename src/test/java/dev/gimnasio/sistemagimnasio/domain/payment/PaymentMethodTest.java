package dev.gimnasio.sistemagimnasio.domain.payment;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentMethodTest {

    @Test
    void elNombre_seRecorta_yTieneUnLargoMaximo() {
        assertEquals("Tarjeta", new PaymentMethod(null, "  Tarjeta ", false, true).name());
        assertEquals(PaymentMethod.MAX_NAME_LENGTH,
                new PaymentMethod(null, "x".repeat(PaymentMethod.MAX_NAME_LENGTH), false, true).name().length());
        assertThrows(DomainException.class,
                () -> new PaymentMethod(null, "x".repeat(PaymentMethod.MAX_NAME_LENGTH + 1), false, true));
    }

    @Test
    void sinNombre_seRechaza() {
        assertThrows(DomainException.class, () -> new PaymentMethod(null, " ", false, true));
    }
}
