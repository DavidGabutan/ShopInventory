package edu.cit.gabutan.inventory;

public interface InventoryService {

    Inventory getItem(String productId);

    Inventory reserve(String productId, int quantity);
}