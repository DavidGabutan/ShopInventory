package edu.cit.gabutan.channel;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "tiangge_orders",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_tiangge_order_id",
            columnNames = "tiangge_order_id"
        )
    }
)
class TianggeOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tiangge_order_id", nullable = false, unique = true)
    private String tianggeOrderId;

    @Column(name = "shop_order_id")
    private Long shopOrderId;

    private String decision;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public String getTianggeOrderId() {
        return tianggeOrderId;
    }

    public void setTianggeOrderId(String tianggeOrderId) {
        this.tianggeOrderId = tianggeOrderId;
    }

    public Long getShopOrderId() {
        return shopOrderId;
    }

    public void setShopOrderId(Long shopOrderId) {
        this.shopOrderId = shopOrderId;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}