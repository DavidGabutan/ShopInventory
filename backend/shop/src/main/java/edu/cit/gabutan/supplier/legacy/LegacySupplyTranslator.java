package edu.cit.gabutan.supplier.legacy;

class LegacySupplyTranslator {

    static String extractSessionToken(String xml) {

        if (xml == null || xml.isBlank()) {
            throw new IllegalArgumentException(
                    "Empty authentication response."
            );
        }

        String token = extract(xml, "SessionToken");

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Session token not found in authentication response."
            );
        }

        return token;
    }

    static LegacyPurchaseOrderAck parsePurchaseOrderAck(String xml) {

        if (xml == null || xml.isBlank()) {
            throw new IllegalArgumentException(
                    "Empty purchase order response."
            );
        }

        String poNumber = extract(xml, "PoNumber");
        String supplierSku = extract(xml, "SupplierSku");
        String qtyText = extract(xml, "Qty");
        String uom = extract(xml, "Uom");
        String buyerRef = extract(xml, "BuyerRef");
        String statusText = extract(xml, "StatusCode");

        if (poNumber == null || poNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Purchase order number not found."
            );
        }

        if (supplierSku == null || supplierSku.isBlank()) {
            throw new IllegalArgumentException(
                    "Supplier SKU not found."
            );
        }

        if (qtyText == null || qtyText.isBlank()) {
            throw new IllegalArgumentException(
                    "Quantity not found."
            );
        }

        if (uom == null || uom.isBlank()) {
            throw new IllegalArgumentException(
                    "Unit of measure not found."
            );
        }

        if (buyerRef == null || buyerRef.isBlank()) {
            throw new IllegalArgumentException(
                    "Buyer reference not found."
            );
        }

        if (statusText == null || statusText.isBlank()) {
            throw new IllegalArgumentException(
                    "Status code not found."
            );
        }

        int qty = Integer.parseInt(qtyText);
        int statusCode = Integer.parseInt(statusText);

        return new LegacyPurchaseOrderAck(
                poNumber,
                supplierSku,
                qty,
                uom,
                buyerRef,
                statusCode
        );
    }

    private static String extract(String xml, String tag) {

        String startTag = "<" + tag + ">";
        String endTag = "</" + tag + ">";

        int start = xml.indexOf(startTag);

        if (start < 0) {
            return null;
        }

        start += startTag.length();

        int end = xml.indexOf(endTag, start);

        if (end < 0) {
            return null;
        }

        return xml.substring(start, end).trim();
    }
}