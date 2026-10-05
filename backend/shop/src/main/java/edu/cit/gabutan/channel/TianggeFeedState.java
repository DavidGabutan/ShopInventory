package edu.cit.gabutan.channel;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tiangge_feed_state")
class TianggeFeedState {

    @Id
    private Short id;

    @Column(name = "cursor_value")
    private String cursorValue;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public String getCursorValue() {
        return cursorValue;
    }

    public void setCursorValue(String cursorValue) {
        this.cursorValue = cursorValue;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}