package edu.cit.gabutan.shop.dto;

import edu.cit.gabutan.inventory.Inventory;

public class OrderResponse {

    private String status;
    private String reason;
    private Inventory inventory;

    public OrderResponse(
            String status,
            String reason,
            Inventory inventory) {

        this.status = status;
        this.reason = reason;
        this.inventory = inventory;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public Inventory getInventory() {
        return inventory;
    }
}