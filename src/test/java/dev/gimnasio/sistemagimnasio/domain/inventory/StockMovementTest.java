package dev.gimnasio.sistemagimnasio.domain.inventory;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StockMovementTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 4, 10, 30);

    /** Un conteo físico puede encontrar cero piezas. */
    @Test
    void ajusteACero_esValido() {
        assertEquals(0, new StockMovement(null, NOW, 1, MovementType.AJUSTE, 0, 1, 1).quantity());
    }

    @ParameterizedTest
    @EnumSource(value = MovementType.class, names = {"ENTRADA", "SALIDA"})
    void entradaOSalidaSinPiezas_seRechaza(MovementType type) {
        assertThrows(DomainException.class, () -> new StockMovement(null, NOW, 1, type, 0, 1, 1));
        assertEquals(1, new StockMovement(null, NOW, 1, type, 1, 1, 1).quantity());
    }

    @ParameterizedTest
    @EnumSource(MovementType.class)
    void cantidadNegativa_seRechaza(MovementType type) {
        assertThrows(DomainException.class, () -> new StockMovement(null, NOW, 1, type, -1, 1, 1));
    }

    @Test
    void movimientoSinTipo_seRechaza() {
        assertThrows(DomainException.class, () -> new StockMovement(null, NOW, 1, null, 5, 1, 1));
    }
}
