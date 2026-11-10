package dev.gimnasio.sistemagimnasio.infrastructure.persistence;

import dev.gimnasio.sistemagimnasio.application.port.out.SettingsRepository;
import dev.gimnasio.sistemagimnasio.domain.settings.GymSettings;
import org.jdbi.v3.core.Jdbi;

import java.util.Currency;
import java.util.Objects;

public final class PostgresSettingsRepository implements SettingsRepository {
    private static final String MISSING = "Falta la configuración del gimnasio.";

    private final Jdbi jdbi;

    public PostgresSettingsRepository(Jdbi jdbi) {
        this.jdbi = Objects.requireNonNull(jdbi, "jdbi");
    }

    @Override
    public GymSettings get() {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT nombre_gimnasio,
                               direccion,
                               telefono,
                               logotipo,
                               leyenda_recibo,
                               moneda,
                               dias_aviso,
                               dias_baja_automatica
                        FROM configuracion
                        WHERE id = 1
                        """)
                .map((rs, ctx) -> new GymSettings(
                        rs.getString("nombre_gimnasio"),
                        rs.getString("direccion"),
                        rs.getString("telefono"),
                        rs.getBytes("logotipo"),
                        rs.getString("leyenda_recibo"),
                        Currency.getInstance(rs.getString("moneda").strip()),
                        rs.getInt("dias_aviso"),
                        rs.getInt("dias_baja_automatica")))
                .findOne()
                .orElseThrow(() -> new IllegalStateException(MISSING)));
    }

    @Override
    public void save(GymSettings settings) {
        int updated = jdbi.withHandle(handle -> handle.createUpdate("""
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
                .bind("baja", settings.autoDeactivationDays())
                .execute());
        if (updated != 1) {
            throw new IllegalStateException(MISSING);
        }
    }
}
