package edu.cit.gabutan.notification;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    private String message;

    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(String message) {
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}