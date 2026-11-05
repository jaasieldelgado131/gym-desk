package dev.gimnasio.sistemagimnasio.domain.sales;

import dev.gimnasio.sistemagimnasio.domain.payment.Folio;
import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SaleTest {
    private static final Folio FOLIO = Folio.of("GYM-V-", 1);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 4, 18, 5);
    private static final SaleLine AGUA = new SaleLine(9L, "Agua 1 L", 2, Money.of("18.50"));
    private static final SaleLine BARRA = new SaleLine(10L, "Barra proteica", 1, Money.of("35.00"));

    @Test
    void elTotal_esLaSumaDeLosSubtotales() {
        assertEquals(Money.of("72.00"), new Sale(null, FOLIO, NOW, List.of(AGUA, BARRA), 1, 1).total());
    }

    @Test
    void ventaSinProductos_seRechaza() {
        assertThrows(DomainException.class, () -> new Sale(null, FOLIO, NOW, List.of(), 1, 1));
        assertThrows(DomainException.class, () -> new Sale(null, FOLIO, NOW, null, 1, 1));
    }

    @Test
    void modificarLaListaOriginal_noAlteraLaVentaRegistrada() {
        List<SaleLine> ticket = new ArrayList<>(List.of(AGUA));
        Sale sale = new Sale(null, FOLIO, NOW, ticket, 1, 1);
        ticket.add(BARRA);

        assertEquals(List.of(AGUA), sale.lines());
        assertEquals(Money.of("37.00"), sale.total());
        assertThrows(UnsupportedOperationException.class, () -> sale.lines().add(BARRA));
    }

    @Test
    void ventaSinFolio_seRechaza() {
        assertThrows(DomainException.class, () -> new Sale(null, null, NOW, List.of(AGUA), 1, 1));
    }

    @Test
    void ventaSinFecha_seRechaza() {
        assertThrows(DomainException.class, () -> new Sale(null, FOLIO, null, List.of(AGUA), 1, 1));
    }
}
