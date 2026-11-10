package dev.gimnasio.sistemagimnasio.infrastructure.persistence;

import dev.gimnasio.sistemagimnasio.domain.payment.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static dev.gimnasio.sistemagimnasio.infrastructure.persistence.TestDatabase.UNIQUE_VIOLATION;
import static dev.gimnasio.sistemagimnasio.infrastructure.persistence.TestDatabase.assertRejected;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class PostgresPaymentMethodRepositoryTest {
    private PostgresPaymentMethodRepository repository;

    @BeforeEach
    void setUp() {
        TestDatabase.clean();
        repository = new PostgresPaymentMethodRepository(TestDatabase.jdbi());
    }

    private PaymentMethod insert(String name, boolean cash, boolean active) {
        return repository.insert(new PaymentMethod(null, name, cash, active));
    }

    @Test
    void insertar_asignaElId_yGuardaTodosLosCampos() {
        PaymentMethod efectivo = insert("Efectivo", true, true);

        assertEquals(Optional.of(efectivo), repository.findById(efectivo.id()));
        assertEquals(new PaymentMethod(efectivo.id(), "Efectivo", true, true), efectivo);
        assertEquals(Optional.empty(), repository.findById(efectivo.id() + 1));
    }

    @Test
    void listar_ordenaPorNombreSinDistinguirMayusculas_yFiltraInactivos() {
        insert("Tarjeta", false, true);
        insert("vales", false, false);
        insert("Efectivo", true, true);

        assertEquals(List.of("Efectivo", "Tarjeta"),
                repository.findAll(false).stream().map(PaymentMethod::name).toList());
        assertEquals(List.of("Efectivo", "Tarjeta", "vales"),
                repository.findAll(true).stream().map(PaymentMethod::name).toList());
    }

    @Test
    void existePorNombre_sinDistinguirMayusculas_yExcluyendoAlPropio() {
        PaymentMethod tarjeta = insert("Tarjeta", false, true);

        assertTrue(repository.existsByName("TARJETA", null));
        assertFalse(repository.existsByName("TARJETA", tarjeta.id()));
        assertTrue(repository.existsByName("tarjeta", tarjeta.id() + 1));
        assertFalse(repository.existsByName("Efectivo", null));
    }

    @Test
    void cuentaSoloLosActivos() {
        insert("Efectivo", true, true);
        insert("Tarjeta", false, true);
        insert("Vales", false, false);

        assertEquals(2, repository.countActive());
    }

    @Test
    void actualizar_cambiaNombreEfectivoYActivo() {
        PaymentMethod vales = insert("Vales", false, true);

        repository.update(new PaymentMethod(vales.id(), "Vales de despensa", true, false));

        assertEquals(Optional.of(new PaymentMethod(vales.id(), "Vales de despensa", true, false)),
                repository.findById(vales.id()));
    }

    @Test
    void actualizarUnoQueNoExiste_falla() {
        assertThrows(IllegalStateException.class, () -> repository.update(new PaymentMethod(99L, "X", false, true)));
    }

    @Test
    void nombreRepetido_conOtrasMayusculas_loRechazaLaBase() {
        insert("Efectivo", true, true);
        assertRejected(UNIQUE_VIOLATION, () -> insert("EFECTIVO", true, true));
    }
}
