package edu.cit.gabutan.supplier.legacy;

import edu.cit.gabutan.supplier.SupplierGateway;
import edu.cit.gabutan.supplier.SupplierGatewayResult;
import edu.cit.gabutan.supplier.SupplierOrder;
import edu.cit.gabutan.supplier.SupplierOrderRepository;
import edu.cit.gabutan.supplier.SupplierOrderStatus;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
class SupplierGatewayImpl implements SupplierGateway {

    private final LegacySupplyClient client;
    private final SupplierOrderRepository repository;

    SupplierGatewayImpl(
            LegacySupplyClient client,
            SupplierOrderRepository repository) {

        this.client = client;
        this.repository = repository;
    }

    @Override
    public SupplierGatewayResult placeOrder(
            String productId,
            int unitsNeeded) {

        if (unitsNeeded <= 0) {
            throw new IllegalArgumentException(
                    "Units needed must be greater than zero."
            );
        }

        // Find the LegacySupply mapping for our product
        LegacyProductMapping mapping =
                LegacyProductMapper.find(productId);

        if (mapping == null) {
            throw new IllegalArgumentException(
                    "No LegacySupply mapping found for product: "
                            + productId
            );
        }

        // Convert required units into supplier cases
        int cases = (int) Math.ceil(
                (double) unitsNeeded / mapping.packSize()
        );

        int orderedUnits =
                cases * mapping.packSize();

        LocalDateTime now = LocalDateTime.now();

        // Create our own supplier order record
        SupplierOrder order = new SupplierOrder();

        order.setProductId(productId);
        order.setCases(cases);
        order.setUnits(orderedUnits);
        order.setStatus(SupplierOrderStatus.PENDING);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        /*
         * Save first so the database generates the ID.
         */
        order = repository.save(order);

        /*
         * BuyerRef must remain the same for this reorder.
         */
        String buyerRef =
                "RO-" + order.getId();

        /*
         * Request ID must also remain the same
         * when this order is retried.
         */
        String requestId =
                "RO-" + order.getId() + "-" + UUID.randomUUID();

        order.setBuyerRef(buyerRef);
        order.setRequestId(requestId);

        repository.save(order);

        try {

            String response =
                    client.placePurchaseOrder(
                            mapping.supplierSku(),
                            cases,
                            buyerRef,
                            requestId
                    );

            LegacyPurchaseOrderAck ack =
                    LegacySupplyTranslator
                            .parsePurchaseOrderAck(response);

            order.setPoNumber(ack.poNumber());
            order.setStatus(
                    SupplierOrderStatus.SUBMITTED
            );
            order.setUpdatedAt(
                    LocalDateTime.now()
            );

            repository.save(order);

            return new SupplierGatewayResult(
                    true,
                    productId,
                    unitsNeeded,
                    orderedUnits,
                    ack.poNumber(),
                    "Supplier order submitted."
            );

        } catch (Exception e) {

            /*
             * Do NOT delete the order.
             * Keep it PENDING so the scheduled retry
             * can send it later.
             */
            order.setStatus(
                    SupplierOrderStatus.PENDING
            );
            order.setUpdatedAt(
                    LocalDateTime.now()
            );

            repository.save(order);

            return new SupplierGatewayResult(
                    false,
                    productId,
                    unitsNeeded,
                    orderedUnits,
                    null,
                    "Supplier unavailable. Order remains pending."
            );
        }
    }
}