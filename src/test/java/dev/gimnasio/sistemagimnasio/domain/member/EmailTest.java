package dev.gimnasio.sistemagimnasio.domain.member;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailTest {

    @Test
    void correosQueSoloDifierenEnMayusculas_sonElMismo() {
        assertEquals(new Email("ana@gym.mx"), new Email("Ana@GYM.mx"));
        assertEquals("ana@gym.mx", new Email("ANA@GYM.MX").value());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ana", "ana@", "@gym.mx", "ana@gym", "ana gomez@gym.mx", "ana@@gym.mx"})
    void correoMalFormado_seRechaza(String value) {
        assertThrows(DomainException.class, () -> new Email(value));
    }
}
