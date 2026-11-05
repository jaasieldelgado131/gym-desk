package dev.gimnasio.sistemagimnasio.domain.member;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Reglas de vigencia, estado y bajas automáticas. Todo se deriva de las membresías; nada del estado se guarda. */
public final class MembershipRules {
    private MembershipRules() {
    }

    /** La membresía con el vencimiento más lejano, o null si el socio no tiene ninguna. */
    public static Membership latest(List<Membership> memberships) {
        return memberships.stream().max(Comparator.comparing(Membership::end)).orElse(null);
    }

    /**
     * @param warningDays días de aviso: con esos días o menos para vencer, el socio está Por vencer
     * @param graceDays   plazo de baja automática, contado desde el vencimiento
     */
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

    /**
     * Inicio de la nueva membresía (RF-03.7, RF-03.8): si la actual aún no vence, el periodo nuevo se suma al final
     * de la actual; si ya venció (también si está de baja) o no tiene membresía, empieza el día del pago.
     */
    public static LocalDate renewalStart(Membership latest, LocalDate paymentDay) {
        if (latest != null && !latest.end().isBefore(paymentDay)) {
            return latest.end();
        }
        return paymentDay;
    }

    /** Bajas del socio (y sus reactivaciones), de la más antigua a la más reciente. */
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
