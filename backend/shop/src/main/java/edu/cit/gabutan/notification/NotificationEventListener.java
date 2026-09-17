package edu.cit.gabutan.notification;

import edu.cit.gabutan.events.LowStockEvent;
import edu.cit.gabutan.events.OrderPlacedEvent;
import edu.cit.gabutan.events.OrderRejectedEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(
            NotificationService notificationService) {

        this.notificationService = notificationService;
    }

    @EventListener
    public void handleOrderPlaced(
            OrderPlacedEvent event) {

        notificationService.saveMessage(
                "Order O"
                        + event.getOrderId()
                        + " confirmed");
    }

    @EventListener
    public void handleOrderRejected(
            OrderRejectedEvent event) {

        notificationService.saveMessage(
                "Order O"
                        + event.getOrderId()
                        + " rejected");
    }

    @EventListener
    public void handleLowStock(
            LowStockEvent event) {

        notificationService.saveMessage(
                "Reorder needed: "
                        + event.getProductName()
                        + " ("
                        + event.getRemainingStock()
                        + " remaining)");
    }
}