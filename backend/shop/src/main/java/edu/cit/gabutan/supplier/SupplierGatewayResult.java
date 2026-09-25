package edu.cit.gabutan.supplier;

public record SupplierGatewayResult(
        boolean success,
        String productId,
        int unitsRequested,
        int unitsOrdered,
        String poNumber,
        String message
) {
}