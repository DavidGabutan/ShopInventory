package edu.cit.gabutan.supplier;

import edu.cit.gabutan.events.LowStockEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AutoReorderListener {

    private final SupplierGateway supplierGateway;

    public AutoReorderListener(
            SupplierGateway supplierGateway) {

        this.supplierGateway = supplierGateway;
    }

    @EventListener
    public void handleLowStock(
            LowStockEvent event) {

        /*
         * Reorder enough units to bring the product
         * back above the low-stock threshold.
         *
         * The supplier adapter handles pack-size
         * conversion and rounding.
         */
        int unitsNeeded =
                10 - event.getRemainingStock();

        if (unitsNeeded <= 0) {
            return;
        }

        supplierGateway.placeOrder(
                event.getProductId(),
                unitsNeeded);
    }
}