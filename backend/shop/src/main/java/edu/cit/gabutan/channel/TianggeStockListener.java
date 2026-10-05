package edu.cit.gabutan.channel;

import edu.cit.gabutan.events.InventoryChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class TianggeStockListener {

    private final TianggeChannel tianggeChannel;
    private final TianggeStockCoordinator stockCoordinator;

    TianggeStockListener(
            TianggeChannel tianggeChannel,
            TianggeStockCoordinator stockCoordinator) {

        this.tianggeChannel = tianggeChannel;
        this.stockCoordinator = stockCoordinator;
    }

    @EventListener
    void handleInventoryChanged(InventoryChangedEvent event) {

        if (stockCoordinator.isDeferring()) {
            stockCoordinator.defer(
                    event.getProductId(),
                    event.getAvailable());
            return;
        }

        tianggeChannel.publishStock(
                event.getProductId(),
                event.getAvailable());
    }
}