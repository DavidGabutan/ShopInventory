package edu.cit.gabutan.shop.dto;

public class OrderItemOutcome {

    private String productId;
    private String outcome;

    public OrderItemOutcome(
            String productId,
            String outcome) {

        this.productId = productId;
        this.outcome = outcome;
    }

    public String getProductId() {
        return productId;
    }

    public String getOutcome() {
        return outcome;
    }
}