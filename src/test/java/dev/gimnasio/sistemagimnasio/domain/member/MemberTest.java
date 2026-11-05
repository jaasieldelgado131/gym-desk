package dev.gimnasio.sistemagimnasio.domain.member;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberTest {
    private static final Phone PHONE = new Phone("5512345678");
    private static final LocalDate JOINED = LocalDate.of(2026, 11, 4);

    @Test
    void elCorreoEsOpcional() {
        assertNull(new Member(null, "Ana López", PHONE, null, JOINED).email());
    }

    @Test
    void elNombreSeGuardaSinEspaciosAlrededor() {
        assertEquals("Ana López", new Member(null, "  Ana López \n", PHONE, null, JOINED).fullName());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void nombreVacio_seRechaza(String name) {
        assertThrows(DomainException.class, () -> new Member(null, name, PHONE, null, JOINED));
    }

    @Test
    void telefonoYFechaDeAlta_sonObligatorios() {
        assertThrows(DomainException.class, () -> new Member(null, "Ana López", null, null, JOINED));
        assertThrows(DomainException.class, () -> new Member(null, "Ana López", PHONE, null, null));
    }
}
