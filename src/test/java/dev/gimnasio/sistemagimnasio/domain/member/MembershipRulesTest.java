package dev.gimnasio.sistemagimnasio.domain.member;

import dev.gimnasio.sistemagimnasio.domain.shared.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static dev.gimnasio.sistemagimnasio.domain.member.MemberStatus.BAJA;
import static dev.gimnasio.sistemagimnasio.domain.member.MemberStatus.POR_VENCER;
import static dev.gimnasio.sistemagimnasio.domain.member.MemberStatus.SIN_MEMBRESIA;
import static dev.gimnasio.sistemagimnasio.domain.member.MemberStatus.VENCIDA;
import static dev.gimnasio.sistemagimnasio.domain.member.MemberStatus.VIGENTE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MembershipRulesTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 3);
    private static final int WARNING = 7;
    private static final int AUTO_DEACTIVATION_DAYS = 30;
    private static final Plan MENSUAL = new Plan(1L, "Mensual", 30, Money.of("450"), true);

    private static Membership endingOn(LocalDate end) {
        return new Membership(null, 1, 1, 1, end.minusDays(30), end);
    }

    private static Membership between(LocalDate start, LocalDate end) {
        return new Membership(null, 1, 1, 1, start, end);
    }

    private static MemberStatus status(Membership latest) {
        return MembershipRules.statusOf(latest, TODAY, WARNING, AUTO_DEACTIVATION_DAYS);
    }

    private static MemberStatus statusOn(List<Membership> history, LocalDate day) {
        return MembershipRules.statusOf(MembershipRules.latest(history).orElse(null), day, WARNING, AUTO_DEACTIVATION_DAYS);
    }


    @Test
    void sinMembresias_estaSinMembresia() {
        assertEquals(Optional.empty(), MembershipRules.latest(List.of()));
        assertEquals(SIN_MEMBRESIA, status(null));
    }

    @Test
    void porVencer_empiezaJustoAlFaltarLosDiasDeAviso() {
        assertEquals(VIGENTE, status(endingOn(TODAY.plusDays(WARNING + 1))));
        assertEquals(POR_VENCER, status(endingOn(TODAY.plusDays(WARNING))));
    }

    @Test
    void elDiaDelVencimiento_aunNoEstaVencida_yElSiguienteSi() {
        assertEquals(POR_VENCER, status(endingOn(TODAY)));
        assertEquals(VENCIDA, status(endingOn(TODAY.minusDays(1))));
    }

    @Test
    void conCeroDiasDeAviso_soloElDiaDelVencimientoEsPorVencer() {
        assertEquals(VIGENTE, MembershipRules.statusOf(endingOn(TODAY.plusDays(1)), TODAY, 0, AUTO_DEACTIVATION_DAYS));
        assertEquals(POR_VENCER, MembershipRules.statusOf(endingOn(TODAY), TODAY, 0, AUTO_DEACTIVATION_DAYS));
    }

    @Test
    void bajaAutomatica_ocurreAlCumplirseElPlazo_niUnDiaAntes() {
        assertEquals(VENCIDA, status(endingOn(TODAY.minusDays(AUTO_DEACTIVATION_DAYS - 1))));
        assertEquals(BAJA, status(endingOn(TODAY.minusDays(AUTO_DEACTIVATION_DAYS))));
    }

    @Test
    void conPlazoDeUnDia_elDiaSiguienteAlVencimientoYaEsBaja() {
        assertEquals(BAJA, MembershipRules.statusOf(endingOn(TODAY.minusDays(1)), TODAY, WARNING, 1));
    }

    @Test
    void elEstadoUsaLaMembresiaQueVenceMasTarde_noLaUltimaDeLaLista() {
        Membership vieja = endingOn(TODAY.minusDays(100));
        Membership actual = between(TODAY.minusDays(20), TODAY.plusDays(5));
        Membership adelantada = between(TODAY.plusDays(5), TODAY.plusDays(35));

        assertEquals(Optional.of(adelantada), MembershipRules.latest(List.of(adelantada, vieja, actual)));
        assertEquals(Optional.of(adelantada), MembershipRules.latest(List.of(actual, vieja, adelantada)));
        assertEquals(VIGENTE, statusOn(List.of(adelantada, actual, vieja), TODAY));
    }


    @Test
    void renovarAntesDeVencer_sumaElPeriodoAlFinalDelActual() {
        Membership current = endingOn(TODAY.plusDays(3));
        assertEquals(current.end(), MembershipRules.renewalStart(current, TODAY));
    }

    @Test
    void renovarElMismoDiaDelVencimiento_empiezaEseDia() {
        assertEquals(TODAY, MembershipRules.renewalStart(endingOn(TODAY), TODAY));
    }

    @Test
    void renovarUnDiaDespuesDeVencer_empiezaElDiaDelPago() {
        assertEquals(TODAY, MembershipRules.renewalStart(endingOn(TODAY.minusDays(1)), TODAY));
    }

    @Test
    void renovarVencidaJustoAntesDeLaBaja_empiezaElDiaDelPago() {
        assertEquals(TODAY, MembershipRules.renewalStart(endingOn(TODAY.minusDays(AUTO_DEACTIVATION_DAYS - 1)), TODAY));
    }

    @Test
    void reactivarDesdeBaja_empiezaElDiaDelPago() {
        assertEquals(TODAY, MembershipRules.renewalStart(endingOn(TODAY.minusDays(AUTO_DEACTIVATION_DAYS + 10)), TODAY));
    }

    @Test
    void primeraMembresia_empiezaElDiaDelPago() {
        assertEquals(TODAY, MembershipRules.renewalStart(null, TODAY));
    }

    @Test
    void renovarConUnaMembresiaYaAdelantada_seSumaAlFinalDeLaMasLejana() {
        Membership actual = between(TODAY.minusDays(20), TODAY.plusDays(10));
        Membership adelantada = between(TODAY.plusDays(10), TODAY.plusDays(40));
        assertEquals(adelantada.end(),
                MembershipRules.renewalStart(MembershipRules.latest(List.of(actual, adelantada)).orElseThrow(), TODAY));
    }

    @Test
    void renovarPorAdelantadoMuchasVeces_noPierdeNingunDia() {
        Random random = new Random(20261104L);
        int[] durations = {1, 30, 90, 180, 365};
        LocalDate firstStart = LocalDate.of(2026, 1, 1);
        Membership current = Membership.begin(1, MENSUAL, 1, firstStart);
        long paidDays = MENSUAL.durationDays();

        for (int i = 0; i < 300; i++) {
            long length = ChronoUnit.DAYS.between(current.start(), current.end());
            LocalDate payDay = current.start().plusDays(random.nextInt((int) length + 1));
            Plan plan = new Plan(2L, "Plan", durations[random.nextInt(durations.length)], Money.of("100"), true);

            current = Membership.begin(1, plan, 1, MembershipRules.renewalStart(current, payDay));
            paidDays += plan.durationDays();
        }
        assertEquals(firstStart.plusDays(paidDays), current.end());
    }

    @Test
    void pagarVencido_cubreElPlanCompletoDesdeElDiaDelPago() {
        Membership vencida = endingOn(TODAY.minusDays(12));
        Membership nueva = Membership.begin(1, MENSUAL, 1, MembershipRules.renewalStart(vencida, TODAY));
        assertEquals(TODAY, nueva.start());
        assertEquals(TODAY.plusDays(30), nueva.end());
    }

    @Test
    void cobrarUnMensualASocioVencido_loDejaVigente() {
        Membership vencida = endingOn(TODAY.minusDays(AUTO_DEACTIVATION_DAYS - 1));
        Membership nueva = Membership.begin(1, MENSUAL, 1, MembershipRules.renewalStart(vencida, TODAY));
        assertEquals(VIGENTE, statusOn(List.of(vencida, nueva), TODAY));
    }


    @Test
    void cicloCompleto_altaCobroAvisoVencimientoBajaYReactivacion() {
        LocalDate joined = LocalDate.of(2026, 1, 10);
        List<Membership> history = new ArrayList<>();
        assertEquals(SIN_MEMBRESIA, statusOn(history, joined));

        history.add(Membership.begin(1, MENSUAL, 1,
                MembershipRules.renewalStart(null, joined)));
        LocalDate end = LocalDate.of(2026, 2, 9);
        assertEquals(end, history.getFirst().end());
        assertEquals(VIGENTE, statusOn(history, joined));
        assertEquals(VIGENTE, statusOn(history, end.minusDays(WARNING + 1)));
        assertEquals(POR_VENCER, statusOn(history, end.minusDays(WARNING)));
        assertEquals(POR_VENCER, statusOn(history, end));
        assertEquals(VENCIDA, statusOn(history, end.plusDays(1)));
        assertEquals(VENCIDA, statusOn(history, end.plusDays(AUTO_DEACTIVATION_DAYS - 1)));
        assertEquals(BAJA, statusOn(history, end.plusDays(AUTO_DEACTIVATION_DAYS)));

        // La fecha de baja es el último vencimiento, no el día en que se cumplió el plazo.
        LocalDate payDay = end.plusDays(45);
        assertEquals(List.of(new Deactivation(end, null)), MembershipRules.deactivations(history, payDay, AUTO_DEACTIVATION_DAYS));

        history.add(Membership.begin(1, MENSUAL, 1,
                MembershipRules.renewalStart(MembershipRules.latest(history).orElse(null), payDay)));
        assertEquals(VIGENTE, statusOn(history, payDay));
        assertEquals(List.of(new Deactivation(end, payDay)), MembershipRules.deactivations(history, payDay, AUTO_DEACTIVATION_DAYS));
    }


    @Test
    void renovacionContigua_nuncaEsBaja_niConElPlazoMinimo() {
        Membership enero = between(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        Membership febrero = between(LocalDate.of(2026, 1, 31), LocalDate.of(2026, 3, 2));
        assertEquals(List.of(), MembershipRules.deactivations(List.of(enero, febrero), LocalDate.of(2026, 2, 15), 1));
    }

    @Test
    void huecoDeUnDiaMenosQueElPlazo_noEsBaja_yUnHuecoIgualAlPlazo_si() {
        LocalDate end = LocalDate.of(2026, 1, 31);
        Membership first = between(end.minusDays(30), end);
        Membership late = between(end.plusDays(AUTO_DEACTIVATION_DAYS - 1), end.plusDays(AUTO_DEACTIVATION_DAYS + 29));
        Membership reactivation = between(end.plusDays(AUTO_DEACTIVATION_DAYS), end.plusDays(AUTO_DEACTIVATION_DAYS + 30));
        LocalDate today = end.plusDays(AUTO_DEACTIVATION_DAYS + 5);

        assertEquals(List.of(), MembershipRules.deactivations(List.of(first, late), today, AUTO_DEACTIVATION_DAYS));
        assertEquals(List.of(new Deactivation(end, end.plusDays(AUTO_DEACTIVATION_DAYS))),
                MembershipRules.deactivations(List.of(first, reactivation), today, AUTO_DEACTIVATION_DAYS));
    }

    @Test
    void membresiaContenidaEnOtraMasLarga_noAcortaLaCobertura() {
        Membership anual = between(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        Membership mensualDentro = between(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 3));
        assertEquals(List.of(),
                MembershipRules.deactivations(List.of(anual, mensualDentro), LocalDate.of(2027, 1, 15), AUTO_DEACTIVATION_DAYS));
    }

    @Test
    void cobroRegistradoDosVeces_noGeneraBajasNiCambiaElEstado() {
        Membership m = endingOn(TODAY.plusDays(20));
        List<Membership> duplicated = List.of(m, m);
        assertEquals(List.of(), MembershipRules.deactivations(duplicated, TODAY, AUTO_DEACTIVATION_DAYS));
        assertEquals(VIGENTE, statusOn(duplicated, TODAY));
    }

    @Test
    void elOrdenEnQueLlegaElHistorial_noCambiaLasBajas() {
        List<Membership> history = new ArrayList<>(List.of(
                between(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)),
                between(LocalDate.of(2025, 4, 1), LocalDate.of(2025, 5, 1)),
                between(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 31)),
                between(LocalDate.of(2025, 9, 1), LocalDate.of(2025, 10, 1))));
        List<Deactivation> expected = List.of(
                new Deactivation(LocalDate.of(2025, 1, 31), LocalDate.of(2025, 4, 1)),
                new Deactivation(LocalDate.of(2025, 5, 31), LocalDate.of(2025, 9, 1)),
                new Deactivation(LocalDate.of(2025, 10, 1), null));

        Random random = new Random(3);
        for (int i = 0; i < 25; i++) {
            Collections.shuffle(history, random);
            assertEquals(expected, MembershipRules.deactivations(history, TODAY, AUTO_DEACTIVATION_DAYS), history::toString);
        }
    }

    @Test
    void cambiarElPlazo_recalculaLasBajasAnteriores() {
        List<Membership> history = List.of(
                between(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)),
                between(LocalDate.of(2026, 3, 15), LocalDate.of(2026, 4, 14))); // hueco de 43 días
        LocalDate today = LocalDate.of(2026, 4, 1);

        assertEquals(List.of(new Deactivation(LocalDate.of(2026, 1, 31), LocalDate.of(2026, 3, 15))),
                MembershipRules.deactivations(history, today, 30));
        assertEquals(List.of(), MembershipRules.deactivations(history, today, 60));
    }

    @Test
    void historialesAleatorios_elEstadoBajaCoincideConUnaBajaAbierta() {
        Random random = new Random(42);
        LocalDate base = LocalDate.of(2025, 1, 1);
        for (int i = 0; i < 5_000; i++) {
            List<Membership> history = new ArrayList<>();
            int count = random.nextInt(5);
            for (int k = 0; k < count; k++) {
                LocalDate start = base.plusDays(random.nextInt(600));
                history.add(between(start, start.plusDays(1 + random.nextInt(120))));
            }
            int autoDeactivationDays = 1 + random.nextInt(60);
            LocalDate today = base.plusDays(random.nextInt(800));
            String context = "historial=" + history + " hoy=" + today + " plazo=" + autoDeactivationDays;

            List<Deactivation> bajas = MembershipRules.deactivations(history, today, autoDeactivationDays);
            boolean open = !bajas.isEmpty() && bajas.getLast().reactivatedOn() == null;
            MemberStatus status = MembershipRules.statusOf(MembershipRules.latest(history).orElse(null), today, WARNING, autoDeactivationDays);

            assertEquals(status == BAJA, open, context);
            for (int b = 0; b < bajas.size(); b++) {
                Deactivation baja = bajas.get(b);
                assertTrue(history.stream().anyMatch(m -> m.end().equals(baja.date())), context);
                if (b < bajas.size() - 1) {
                    assertNotNull(baja.reactivatedOn(), context);
                }
                if (baja.reactivatedOn() != null) {
                    assertTrue(!baja.reactivatedOn().isBefore(baja.date().plusDays(autoDeactivationDays)), context);
                }
            }
        }
    }
}
