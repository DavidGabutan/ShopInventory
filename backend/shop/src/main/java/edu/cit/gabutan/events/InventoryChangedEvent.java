package edu.cit.gabutan.events;

public class InventoryChangedEvent {

    private final String productId;
    private final int available;

    public InventoryChangedEvent(
            String productId,
            int available) {

        this.productId = productId;
        this.available = available;
    }

    public String getProductId() {
        return productId;
    }

    public int getAvailable() {
        return available;
    }
}
