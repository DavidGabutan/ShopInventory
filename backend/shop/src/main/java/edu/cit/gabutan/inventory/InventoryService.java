package edu.cit.gabutan.inventory;

import java.util.List;

public interface InventoryService {

    Inventory getItem(String productId);

    List<Inventory> getAll();

    Inventory reserve(String productId, int quantity);

    Inventory restock(String productId, int quantity);
}