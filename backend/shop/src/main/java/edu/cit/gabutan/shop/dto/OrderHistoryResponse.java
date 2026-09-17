package edu.cit.gabutan.shop.dto;

import java.time.LocalDateTime;
import java.util.List;

public class OrderHistoryResponse {

    private Long orderId;
    private String status;
    private String reason;
    private LocalDateTime createdAt;
    private List<OrderItemRequest> items;

    public OrderHistoryResponse(
            Long orderId,
            String status,
            String reason,
            LocalDateTime createdAt,
            List<OrderItemRequest> items) {

        this.orderId = orderId;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
        this.items = items;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }
}