package dev.gimnasio.sistemagimnasio.domain.member;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

import java.time.LocalDate;

public record Membership(Long id, long memberId, long planId, LocalDate start, LocalDate end) {
    public Membership {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new DomainException("El vencimiento debe ser posterior al inicio.");
        }
    }

    public static Membership begin(long memberId, Plan plan, LocalDate start) {
        return new Membership(null, memberId, plan.id(), start, start.plusDays(plan.durationDays()));
    }
}
