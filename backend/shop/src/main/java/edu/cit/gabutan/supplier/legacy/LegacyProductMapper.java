package edu.cit.gabutan.supplier.legacy;

import java.util.Map;

class LegacyProductMapper {

    private static final Map<String, LegacyProductMapping> PRODUCTS =
            Map.of(
                    "P100",
                    new LegacyProductMapping(
                            "P100",
                            "RZT-6368",
                            20
                    ),

                    "P200",
                    new LegacyProductMapping(
                            "P200",
                            "RZT-3795",
                            20
                    ),

                    "P300",
                    new LegacyProductMapping(
                            "P300",
                            "RZT-2377",
                            10
                    )
            );

    static LegacyProductMapping find(String productId) {

        LegacyProductMapping mapping =
                PRODUCTS.get(productId);

        if (mapping == null) {
            throw new IllegalArgumentException(
                    "No supplier mapping for product: " + productId
            );
        }

        return mapping;
    }
}