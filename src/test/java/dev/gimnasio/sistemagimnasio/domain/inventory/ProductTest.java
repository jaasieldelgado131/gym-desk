package dev.gimnasio.sistemagimnasio.domain.inventory;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static dev.gimnasio.sistemagimnasio.domain.inventory.MovementType.AJUSTE;
import static dev.gimnasio.sistemagimnasio.domain.inventory.MovementType.ENTRADA;
import static dev.gimnasio.sistemagimnasio.domain.inventory.MovementType.SALIDA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 4, 10, 30);

    private static Product product(int stock, int minStock) {
        return new Product(1L, "PROT-01", "Proteína 1 kg", 1L, Money.of("850"), minStock, stock, true);
    }

    @Test
    void estaBajo_cuandoLaExistenciaEsIgualOMenorAlMinimo() {
        assertFalse(product(4, 3).isLow());
        assertTrue(product(3, 3).isLow());
        assertTrue(product(0, 3).isLow());
    }

    @Test
    void conMinimoCero_soloEstaBajoSinExistencia() {
        assertFalse(product(1, 0).isLow());
        assertTrue(product(0, 0).isLow());
    }

    @Test
    void entradaSuma_salidaResta_yAjusteFijaLaExistencia() {
        Product p = product(10, 3);
        assertEquals(15, p.stockAfter(ENTRADA, 5));
        assertEquals(7, p.stockAfter(SALIDA, 3));
        assertEquals(4, p.stockAfter(AJUSTE, 4));
        assertEquals(0, p.stockAfter(AJUSTE, 0));
    }

    @Test
    void salidaPuedeAgotarLaExistencia_peroNoDejarlaNegativa() {
        Product p = product(10, 3);
        assertEquals(0, p.stockAfter(SALIDA, 10));
        DomainException e = assertThrows(DomainException.class, () -> p.stockAfter(SALIDA, 11));
        assertTrue(e.getMessage().contains("Proteína 1 kg"), e.getMessage());
    }

    @ParameterizedTest
    @EnumSource(MovementType.class)
    void cantidadNegativa_seRechazaEnCualquierMovimiento(MovementType type) {
        assertThrows(DomainException.class, () -> product(10, 3).stockAfter(type, -1));
    }

    @Test
    void aplicarUnMovimiento_devuelveOtroProductoYNoTocaElOriginal() {
        Product p = product(10, 3);
        Product after = p.apply(SALIDA, 8);
        assertEquals(10, p.stock());
        assertEquals(new Product(1L, "PROT-01", "Proteína 1 kg", 1L, Money.of("850"), 3, 2, true), after);
        assertTrue(after.isLow());
    }

    @Test
    void ventasSucesivas_descuentanAcumulando_yLaQueExcedeSeRechaza() {
        Product agotado = product(5, 1).apply(SALIDA, 2).apply(SALIDA, 3);
        assertEquals(0, agotado.stock());
        assertThrows(DomainException.class, () -> agotado.apply(SALIDA, 1));
    }

    /** RF-09.3: la vista previa debe rechazar lo mismo que rechazará el registro del movimiento. */
    @ParameterizedTest
    @EnumSource(value = MovementType.class, names = {"ENTRADA", "SALIDA"})
    void laVistaPrevia_rechazaLaCantidadCeroIgualQueElRegistro(MovementType type) {
        assertThrows(DomainException.class, () -> new StockMovement(null, NOW, 1, type, 0, 1, 1));
        assertThrows(DomainException.class, () -> product(10, 3).stockAfter(type, 0));
    }

    @Test
    void datosObligatorios_yExistenciasNoNegativas() {
        Money price = Money.of("850");
        assertThrows(DomainException.class, () -> new Product(null, " ", "Proteína", 1, price, 0, 0, true));
        assertThrows(DomainException.class, () -> new Product(null, "PROT-01", "", 1, price, 0, 0, true));
        assertThrows(DomainException.class, () -> new Product(null, "PROT-01", "Proteína", 1, null, 0, 0, true));
        assertThrows(DomainException.class, () -> new Product(null, "PROT-01", "Proteína", 1, price, -1, 0, true));
        assertThrows(DomainException.class, () -> new Product(null, "PROT-01", "Proteína", 1, price, 0, -1, true));
    }

    @Test
    void claveYNombre_seGuardanSinEspaciosAlrededor() {
        Product p = new Product(null, " PROT-01 ", " Proteína ", 1, Money.of("850"), 0, 0, true);
        assertEquals("PROT-01", p.code());
        assertEquals("Proteína", p.name());
    }
}
