package edu.cit.gabutan.events;

public class OrderRejectedEvent {

    private final Long orderId;

    public OrderRejectedEvent(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }
}