package edu.cit.gabutan.channel;

import edu.cit.gabutan.inventory.Inventory;
import edu.cit.gabutan.inventory.InventoryService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class TianggeStartup {

    private final TianggeChannel tianggeChannel;
    private final InventoryService inventoryService;

    TianggeStartup(
            TianggeChannel tianggeChannel,
            InventoryService inventoryService) {

        this.tianggeChannel = tianggeChannel;
        this.inventoryService = inventoryService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startup() {

        // Task 1: first Tiangge call
        tianggeChannel.heartbeat();

        // Task 2: publish listings after heartbeat
        tianggeChannel.publishListings();

        // Task 3: publish current inventory after listings
        List<Inventory> inventory = inventoryService.getAll();
        tianggeChannel.publishCurrentStock(inventory);
    }

    @Scheduled(initialDelay = 30000, fixedRate = 30000)
    void scheduledHeartbeat() {
        tianggeChannel.heartbeat();
    }
}