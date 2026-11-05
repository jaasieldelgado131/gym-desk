package dev.gimnasio.sistemagimnasio.domain.member;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

import java.time.LocalDate;

/** {@code email} puede ser null. El estado no se guarda: se calcula con {@link MembershipRules}. */
public record Member(Long id, String fullName, Phone phone, Email email, LocalDate joinDate) {
    public Member {
        if (fullName == null || fullName.isBlank()) {
            throw new DomainException("El nombre completo es obligatorio.");
        }
        if (phone == null) {
            throw new DomainException("El teléfono es obligatorio.");
        }
        if (joinDate == null) {
            throw new DomainException("La fecha de alta es obligatoria.");
        }
        fullName = fullName.strip();
    }
}
