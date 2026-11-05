package dev.gimnasio.sistemagimnasio.domain.sales;

import dev.gimnasio.sistemagimnasio.domain.inventory.Product;
import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SaleLineTest {

    private static Product product(boolean active) {
        return new Product(9L, "AGUA-1L", "Agua 1 L", 2L, Money.of("18.50"), 5, 40, active);
    }

    @Test
    void tomaDelProductoSuIdNombreYPrecio_yCalculaElSubtotal() {
        SaleLine line = SaleLine.of(product(true), 3);
        assertEquals(new SaleLine(9L, "Agua 1 L", 3, Money.of("18.50")), line);
        assertEquals(Money.of("55.50"), line.subtotal());
    }

    @Test
    void productoInactivo_noSePuedeVender() {
        assertThrows(DomainException.class, () -> SaleLine.of(product(false), 1));
    }

    @Test
    void cantidadMenorAUno_seRechaza() {
        assertThrows(DomainException.class, () -> SaleLine.of(product(true), 0));
        assertThrows(DomainException.class, () -> SaleLine.of(product(true), -2));
    }

    /** El error debe aparecer al armar el renglón, no al calcular el total. */
    @Test
    void renglonSinPrecio_seRechazaAlCrearlo() {
        assertThrows(DomainException.class, () -> new SaleLine(9L, "Agua 1 L", 1, null));
    }
}
