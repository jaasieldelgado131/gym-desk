package dev.gimnasio.sistemagimnasio.domain.member;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhoneTest {

    @Test
    void diezDigitos_seAceptanTalCual() {
        assertEquals("5512345678", new Phone("5512345678").value());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "551234567",
            "55123456789",
            "+525512345678",
            "551234567a",
            "５５１２３４５６７８", // dígitos no ASCII: romperían la búsqueda por teléfono
            "٥٥١٢٣٤٥٦٧٨"
    })
    void loQueNoSonDiezDigitosAscii_seRechaza(String value) {
        assertThrows(DomainException.class, () -> new Phone(value));
    }
}
