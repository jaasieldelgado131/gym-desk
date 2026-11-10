package dev.gimnasio.sistemagimnasio.infrastructure.persistence;

import dev.gimnasio.sistemagimnasio.application.port.out.SettingsRepository;
import dev.gimnasio.sistemagimnasio.domain.settings.GymSettings;
import org.jdbi.v3.core.Jdbi;

import java.util.Currency;
import java.util.Objects;

public final class PostgresSettingsRepository implements SettingsRepository {
    private static final String SERIES = "COBRO";
    private static final String MISSING = "Falta la configuración del gimnasio o la serie de folios " + SERIES + ".";

    private final Jdbi jdbi;

    public PostgresSettingsRepository(Jdbi jdbi) {
        this.jdbi = Objects.requireNonNull(jdbi, "jdbi");
    }

    @Override
    public GymSettings get() {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT c.nombre_gimnasio,
                               c.direccion,
                               c.telefono,
                               c.logotipo,
                               c.leyenda_recibo,
                               c.moneda,
                               c.dias_aviso,
                               c.dias_baja_automatica,
                               f.prefijo
                        FROM configuracion c
                        JOIN folio_secuencia f ON f.serie = :serie
                        WHERE c.id = 1
                        """)
                .bind("serie", SERIES)
                .map((rs, ctx) -> new GymSettings(
                        rs.getString("nombre_gimnasio"),
                        rs.getString("direccion"),
                        rs.getString("telefono"),
                        rs.getBytes("logotipo"),
                        rs.getString("leyenda_recibo"),
                        Currency.getInstance(rs.getString("moneda").strip()),
                        rs.getInt("dias_aviso"),
                        rs.getInt("dias_baja_automatica"),
                        rs.getString("prefijo")))
                .findOne()
                .orElseThrow(() -> new IllegalStateException(MISSING)));
    }

    @Override
    public void save(GymSettings settings) {
        jdbi.useTransaction(handle -> {
            int updated = handle.createUpdate("""
                            UPDATE configuracion
                            SET nombre_gimnasio      = :nombre,
                                direccion            = :direccion,
                                telefono             = :telefono,
                                logotipo             = :logotipo,
                                leyenda_recibo       = :leyenda,
                                moneda               = :moneda,
                                dias_aviso           = :aviso,
                                dias_baja_automatica = :baja
                            WHERE id = 1
                            """)
                    .bind("nombre", settings.gymName())
                    .bind("direccion", settings.address())
                    .bind("telefono", settings.phone())
                    .bind("logotipo", settings.logo())
                    .bind("leyenda", settings.receiptFooter())
                    .bind("moneda", settings.currency().getCurrencyCode())
                    .bind("aviso", settings.warningDays())
                    .bind("baja", settings.graceDays())
                    .execute();
            int prefixUpdated = handle.createUpdate("""
                            UPDATE folio_secuencia
                            SET prefijo = :prefijo
                            WHERE serie = :serie
                            """)
                    .bind("prefijo", settings.folioPrefix())
                    .bind("serie", SERIES)
                    .execute();
            if (updated != 1 || prefixUpdated != 1) {
                throw new IllegalStateException(MISSING);
            }
        });
    }

    @Override
    public int folioWidth() {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT ancho
                        FROM folio_secuencia
                        WHERE serie = :serie
                        """)
                .bind("serie", SERIES)
                .mapTo(Integer.class)
                .findOne()
                .orElseThrow(() -> new IllegalStateException(MISSING)));
    }

    @Override
    public int countPayments() {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT count(*)
                        FROM pago
                        """)
                .mapTo(Integer.class)
                .one());
    }

    @Override
    public int countSales() {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT count(*)
                        FROM venta
                        """)
                .mapTo(Integer.class)
                .one());
    }
}
