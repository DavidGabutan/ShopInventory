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
                    "Units needed must be greater than zero.");
        }

        LegacyProductMapping mapping =
                LegacyProductMapper.find(productId);

        int cases = (int) Math.ceil(
                (double) unitsNeeded / mapping.packSize());

        int orderedUnits =
                cases * mapping.packSize();

        /*
         * Create the local supplier order first.
         * Temporary non-null values satisfy the database constraints.
         */
        SupplierOrder order =
                new SupplierOrder();

        order.setProductId(productId);

        order.setBuyerRef(
                "TEMP-" + UUID.randomUUID());

        order.setRequestId(
                UUID.randomUUID().toString());

        order.setCases(cases);
        order.setUnits(orderedUnits);

        order.setStatus(
                SupplierOrderStatus.PENDING);

        order.setCreatedAt(
                LocalDateTime.now());

        order.setUpdatedAt(
                LocalDateTime.now());

        /*
         * Save and flush so the database generates the ID.
         */
        order = repository.saveAndFlush(order);

        /*
         * BuyerRef must use our supplier_orders ID.
         */
        String buyerRef =
                "RO-" + order.getId();

        /*
         * Keep the same request ID for this reorder.
         */
        String requestId =
                "RO-" + order.getId() + "-" +
                UUID.randomUUID();

        order.setBuyerRef(buyerRef);
        order.setRequestId(requestId);
        order.setUpdatedAt(LocalDateTime.now());

        order = repository.saveAndFlush(order);

        try {

            String response =
                    client.placePurchaseOrder(
                            mapping.supplierSku(),
                            cases,
                            buyerRef,
                            requestId);

            LegacyPurchaseOrderAck ack =
                    LegacySupplyTranslator
                            .parsePurchaseOrderAck(response);

            order.setPoNumber(
                    ack.poNumber());

            order.setStatus(
                    SupplierOrderStatus.SUBMITTED);

            order.setUpdatedAt(
                    LocalDateTime.now());

            repository.save(order);

            return new SupplierGatewayResult(
                    true,
                    productId,
                    unitsNeeded,
                    orderedUnits,
                    ack.poNumber(),
                    "Supplier order submitted.");

        } catch (Exception e) {
                e.printStackTrace();
            /*
             * LegacySupply was unavailable or rejected
             * the request. Keep the reorder pending.
             */
            order.setStatus(
                    SupplierOrderStatus.PENDING);

            order.setUpdatedAt(
                    LocalDateTime.now());

            repository.save(order);

            return new SupplierGatewayResult(
                    false,
                    productId,
                    unitsNeeded,
                    orderedUnits,
                    null,
                    "Supplier unavailable. Order remains pending.");
        }
    }
}