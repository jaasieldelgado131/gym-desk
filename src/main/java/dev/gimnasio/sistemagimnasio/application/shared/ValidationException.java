package dev.gimnasio.sistemagimnasio.application.shared;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Errores por campo, en el orden en que se revisaron. */
public class ValidationException extends DomainException {
    private final Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super(joinMessages(errors));
        this.errors = Collections.unmodifiableMap(new LinkedHashMap<>(errors));
    }

    public static ValidationException of(String field, String message) {
        return new ValidationException(Map.of(field, message));
    }

    public Map<String, String> errors() {
        return errors;
    }

    private static String joinMessages(Map<String, String> errors) {
        if (errors == null || errors.isEmpty()) {
            throw new IllegalArgumentException("errors");
        }
        return String.join(" ", errors.values());
    }
}
