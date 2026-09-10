package edu.cit.gabutan.inventory;

import org.springframework.stereotype.Service;

@Service
class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public Inventory getItem(String productId) {

        return inventoryRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));
    }

    @Override
    public Inventory reserve(String productId, int quantity) {

        if (quantity <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than zero");
        }

        Inventory item = getItem(productId);

        if (quantity > item.getStock()) {
            throw new RuntimeException(
                    "Insufficient stock");
        }

        item.setStock(item.getStock() - quantity);

        return inventoryRepository.save(item);
    }
}