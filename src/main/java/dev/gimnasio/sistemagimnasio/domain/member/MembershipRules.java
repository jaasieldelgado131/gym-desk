package dev.gimnasio.sistemagimnasio.domain.member;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Todo se deriva de las membresías; ni el estado ni las bajas se guardan. */
public final class MembershipRules {
    private MembershipRules() {
    }

    /** null si no hay membresías. */
    public static Membership latest(List<Membership> memberships) {
        return memberships.stream().max(Comparator.comparing(Membership::end)).orElse(null);
    }

    /** @param graceDays días después del vencimiento para pasar a baja */
    public static MemberStatus statusOf(Membership latest, LocalDate today, int warningDays, int graceDays) {
        if (latest == null) {
            return MemberStatus.SIN_MEMBRESIA;
        }
        long daysLeft = ChronoUnit.DAYS.between(today, latest.end());
        if (daysLeft >= 0) {
            return daysLeft <= warningDays ? MemberStatus.POR_VENCER : MemberStatus.VIGENTE;
        }
        return -daysLeft >= graceDays ? MemberStatus.BAJA : MemberStatus.VENCIDA;
    }

    /** Si la actual no ha vencido, la nueva empieza cuando termina; si ya venció o no hay, el día del pago. */
    public static LocalDate renewalStart(Membership latest, LocalDate paymentDay) {
        if (latest != null && !latest.end().isBefore(paymentDay)) {
            return latest.end();
        }
        return paymentDay;
    }

    /** De la más antigua a la más reciente. */
    public static List<Deactivation> deactivations(List<Membership> memberships, LocalDate today, int graceDays) {
        List<Membership> sorted = memberships.stream().sorted(Comparator.comparing(Membership::start)).toList();
        List<Deactivation> result = new ArrayList<>();
        LocalDate coveredUntil = null;
        for (Membership m : sorted) {
            if (coveredUntil != null && ChronoUnit.DAYS.between(coveredUntil, m.start()) >= graceDays) {
                result.add(new Deactivation(coveredUntil, m.start()));
            }
            if (coveredUntil == null || m.end().isAfter(coveredUntil)) {
                coveredUntil = m.end();
            }
        }
        if (coveredUntil != null && ChronoUnit.DAYS.between(coveredUntil, today) >= graceDays) {
            result.add(new Deactivation(coveredUntil, null));
        }
        return result;
    }
}
