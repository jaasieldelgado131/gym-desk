package dev.gimnasio.sistemagimnasio.domain.sales;

import dev.gimnasio.sistemagimnasio.domain.inventory.Product;
import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;

/** Guarda el precio del momento de la venta, no el actual del producto. */
public record SaleLine(long productId, String productName, int quantity, Money unitPrice) {
    public SaleLine {
        if (quantity < 1) {
            throw new DomainException("La cantidad debe ser de al menos 1.");
        }
        if (unitPrice == null) {
            throw new DomainException("El renglón debe tener su precio.");
        }
    }

    public static SaleLine of(Product product, int quantity) {
        if (!product.active()) {
            throw new DomainException("El producto \"" + product.name() + "\" está inactivo.");
        }
        return new SaleLine(product.id(), product.name(), quantity, product.price());
    }

    public Money subtotal() {
        return unitPrice.times(quantity);
    }
}
