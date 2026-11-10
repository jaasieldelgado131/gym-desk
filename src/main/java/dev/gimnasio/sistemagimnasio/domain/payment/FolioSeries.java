package dev.gimnasio.sistemagimnasio.domain.payment;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

import java.util.regex.Pattern;

/** Una numeración de folios: su prefijo y cuántos dígitos se rellenan con ceros. */
public record FolioSeries(String series, String prefix, int width) {
    public static final String MEMBERSHIP_PAYMENTS = "COBRO";
    private static final Pattern PREFIX = Pattern.compile("[A-Z0-9-]{1,8}");

    public FolioSeries {
        if (series == null || series.isBlank()) {
            throw new DomainException("La serie de folios es obligatoria.");
        }
        if (prefix == null || !PREFIX.matcher(prefix).matches()) {
            throw new DomainException("El prefijo de folios debe tener de 1 a 8 letras mayúsculas, números o guiones.");
        }
        if (width < 1) {
            throw new DomainException("El ancho del folio debe ser de al menos 1 dígito.");
        }
    }

    public static boolean isValidPrefix(String prefix) {
        return prefix != null && PREFIX.matcher(prefix).matches();
    }

    public FolioSeries withPrefix(String prefix) {
        return new FolioSeries(series, prefix, width);
    }

    public Folio format(long sequence) {
        return Folio.of(prefix, sequence, width);
    }
}
