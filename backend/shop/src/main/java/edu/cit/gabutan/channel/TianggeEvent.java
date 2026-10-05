package edu.cit.gabutan.channel;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "tiangge_events",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_tiangge_event_id", columnNames = "event_id")
    }
)
class TianggeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "sequence_number")
    private Long sequenceNumber;

    @Column(name = "tiangge_order_id")
    private String tianggeOrderId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "processing_status", nullable = false)
    private String processingStatus;

    @Column(name = "shop_order_id")
    private Long shopOrderId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getTianggeOrderId() {
        return tianggeOrderId;
    }

    public void setTianggeOrderId(String tianggeOrderId) {
        this.tianggeOrderId = tianggeOrderId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    public Long getShopOrderId() {
        return shopOrderId;
    }

    public void setShopOrderId(Long shopOrderId) {
        this.shopOrderId = shopOrderId;
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
