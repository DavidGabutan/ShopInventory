package edu.cit.gabutan.inventory;

import edu.cit.gabutan.events.LowStockEvent;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class InventoryServiceImpl implements InventoryService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final InventoryRepository inventoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    InventoryServiceImpl(
            InventoryRepository inventoryRepository,
            ApplicationEventPublisher eventPublisher) {

        this.inventoryRepository = inventoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Inventory getItem(String productId) {

        return inventoryRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));
    }

    @Override
    public List<Inventory> getAll() {
        return inventoryRepository.findAll();
    }

    @Override
    public Inventory reserve(
            String productId,
            int quantity) {

        if (quantity <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than zero");
        }

        Inventory item = getItem(productId);

        if (quantity > item.getStock()) {
            throw new RuntimeException("Insufficient stock");
        }

        item.setStock(item.getStock() - quantity);

        Inventory updated =
                inventoryRepository.save(item);

        if (updated.getStock() < LOW_STOCK_THRESHOLD) {

            eventPublisher.publishEvent(
                    new LowStockEvent(
                            updated.getProductId(),
                            updated.getName(),
                            updated.getStock()));
        }

        return updated;
    }

    @Override
    public Inventory restock(
            String productId,
            int quantity) {

        if (quantity <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than zero");
        }

        Inventory item = getItem(productId);

        item.setStock(
                item.getStock() + quantity);

        return inventoryRepository.save(item);
    }
}