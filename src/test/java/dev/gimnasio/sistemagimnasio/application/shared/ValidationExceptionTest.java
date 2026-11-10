package dev.gimnasio.sistemagimnasio.application.shared;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationExceptionTest {

    @Test
    void conservaElOrdenDeLosCampos_yUneLosMensajes() {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("username", "Escribe tu nombre de usuario.");
        errors.put("password", "Escribe tu contraseña.");

        ValidationException e = new ValidationException(errors);

        assertEquals(List.of("username", "password"), List.copyOf(e.errors().keySet()));
        assertEquals("Escribe tu nombre de usuario. Escribe tu contraseña.", e.getMessage());
    }

    @Test
    void losErrores_noCambianSiSeModificaElMapaOriginal_niSePuedenModificar() {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("name", "Escribe el nombre.");
        ValidationException e = new ValidationException(errors);

        errors.put("otro", "x");

        assertEquals(Map.of("name", "Escribe el nombre."), e.errors());
        assertThrows(UnsupportedOperationException.class, () -> e.errors().put("x", "y"));
    }

    @Test
    void sinErrores_noSeConstruye() {
        assertThrows(IllegalArgumentException.class, () -> new ValidationException(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new ValidationException(null));
    }

    @Test
    void deUnSoloCampo() {
        assertEquals(Map.of("name", "Escribe el nombre."), ValidationException.of("name", "Escribe el nombre.").errors());
    }
}
