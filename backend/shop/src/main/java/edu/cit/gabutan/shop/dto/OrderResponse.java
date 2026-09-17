package edu.cit.gabutan.shop.dto;

import edu.cit.gabutan.inventory.Inventory;

import java.util.List;

public class OrderResponse {

    private String status;
    private String reason;
    private List<OrderItemOutcome> items;
    private Inventory inventory;

    public OrderResponse(
            String status,
            String reason,
            List<OrderItemOutcome> items,
            Inventory inventory) {

        this.status = status;
        this.reason = reason;
        this.items = items;
        this.inventory = inventory;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public List<OrderItemOutcome> getItems() {
        return items;
    }

    public Inventory getInventory() {
        return inventory;
    }
}