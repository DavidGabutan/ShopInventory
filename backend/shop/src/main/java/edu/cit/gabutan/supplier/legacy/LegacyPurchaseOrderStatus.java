package edu.cit.gabutan.supplier.legacy;

record LegacyPurchaseOrderStatus(
        String poNumber,
        String supplierSku,
        int qty,
        String uom,
        String buyerRef,
        int statusCode
) {
}