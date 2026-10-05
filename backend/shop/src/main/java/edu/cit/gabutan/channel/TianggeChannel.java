package edu.cit.gabutan.channel;

import edu.cit.gabutan.inventory.Inventory;

import java.util.List;

public interface TianggeChannel {

    void heartbeat();

    void publishListings();

    void publishStock(String sellerSku, int available);

    void publishCurrentStock(List<Inventory> inventory);
}