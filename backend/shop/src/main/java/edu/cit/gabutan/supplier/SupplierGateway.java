package edu.cit.gabutan.supplier;

public interface SupplierGateway {

    SupplierGatewayResult placeOrder(
            String productId,
            int unitsNeeded
    );
}