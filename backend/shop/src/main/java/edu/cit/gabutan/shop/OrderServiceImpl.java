package edu.cit.gabutan.shop;

import edu.cit.gabutan.inventory.Inventory;
import edu.cit.gabutan.inventory.InventoryService;
import edu.cit.gabutan.shop.dto.OrderRequest;
import edu.cit.gabutan.shop.dto.OrderResponse;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderServiceImpl implements OrderService {

    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;

    public OrderServiceImpl(
            InventoryService inventoryService,
            OrderRepository orderRepository) {

        this.inventoryService = inventoryService;
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderResponse placeOrder(OrderRequest request) {

        Inventory inventory =
                inventoryService.getItem(request.getProductId());

        try {

            Inventory updatedInventory =
                    inventoryService.reserve(
                            request.getProductId(),
                            request.getQuantity());

            Order order = new Order();

            order.setProductId(request.getProductId());
            order.setQuantity(request.getQuantity());
            order.setStatus("CONFIRMED");
            order.setReason("Order confirmed");
            order.setCreatedAt(LocalDateTime.now());

            orderRepository.save(order);

            return new OrderResponse(
                    "CONFIRMED",
                    "Order confirmed",
                    updatedInventory);

        } catch (RuntimeException e) {

            Order order = new Order();

            order.setProductId(request.getProductId());
            order.setQuantity(request.getQuantity());
            order.setStatus("REJECTED");
            order.setReason(e.getMessage());
            order.setCreatedAt(LocalDateTime.now());

            orderRepository.save(order);

            return new OrderResponse(
                    "REJECTED",
                    e.getMessage(),
                    inventory);
        }
    }
}