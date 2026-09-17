package edu.cit.gabutan.shop;

import edu.cit.gabutan.events.OrderPlacedEvent;
import edu.cit.gabutan.events.OrderRejectedEvent;
import edu.cit.gabutan.inventory.Inventory;
import edu.cit.gabutan.inventory.InventoryService;
import edu.cit.gabutan.shop.dto.OrderHistoryResponse;
import edu.cit.gabutan.shop.dto.OrderItemOutcome;
import edu.cit.gabutan.shop.dto.OrderItemRequest;
import edu.cit.gabutan.shop.dto.OrderRequest;
import edu.cit.gabutan.shop.dto.OrderResponse;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderServiceImpl(
            InventoryService inventoryService,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ApplicationEventPublisher eventPublisher) {

        this.inventoryService = inventoryService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(
            OrderRequest request) {

        if (request.getItems() == null
                || request.getItems().isEmpty()) {

            throw new RuntimeException(
                    "Order must contain at least one item");
        }

        /*
         * STEP 1:
         * Validate every item BEFORE reserving anything.
         */
        for (OrderItemRequest item : request.getItems()) {

            if (item.getQuantity() <= 0) {
                throw new RuntimeException(
                        "Quantity must be greater than zero");
            }

            Inventory inventory =
                    inventoryService.getItem(
                            item.getProductId());

            if (item.getQuantity()
                    > inventory.getStock()) {

                Order rejectedOrder = new Order();

                rejectedOrder.setStatus("REJECTED");

                rejectedOrder.setReason(
                        "Insufficient stock for "
                                + item.getProductId());

                rejectedOrder.setCreatedAt(
                        LocalDateTime.now());

                orderRepository.save(rejectedOrder);

                List<OrderItemOutcome> outcomes =
                        new ArrayList<>();

                for (OrderItemRequest rejectedItem
                        : request.getItems()) {

                    outcomes.add(
                            new OrderItemOutcome(
                                    rejectedItem.getProductId(),
                                    "NOT_RESERVED"));
                }

                eventPublisher.publishEvent(
                        new OrderRejectedEvent(
                                rejectedOrder.getOrderId()));

                return new OrderResponse(
                        "REJECTED",
                        "Insufficient stock for "
                                + item.getProductId(),
                        outcomes,
                        inventory);
            }
        }

        /*
         * STEP 2:
         * Every item passed validation.
         * Only now do we reserve stock.
         */
        Order order = new Order();

        order.setStatus("CONFIRMED");
        order.setReason("Order confirmed");
        order.setCreatedAt(LocalDateTime.now());

        orderRepository.save(order);

        try {

            List<OrderItemOutcome> outcomes =
                    new ArrayList<>();

            Inventory lastInventory = null;

            for (OrderItemRequest item
                    : request.getItems()) {

                lastInventory =
                        inventoryService.reserve(
                                item.getProductId(),
                                item.getQuantity());

                OrderItem orderItem =
                        new OrderItem();

                orderItem.setOrderId(
                        order.getOrderId());

                orderItem.setProductId(
                        item.getProductId());

                orderItem.setQuantity(
                        item.getQuantity());

                orderItemRepository.save(
                        orderItem);

                outcomes.add(
                        new OrderItemOutcome(
                                item.getProductId(),
                                "RESERVED"));
            }

            eventPublisher.publishEvent(
                    new OrderPlacedEvent(
                            order.getOrderId()));

            return new OrderResponse(
                    "CONFIRMED",
                    "Order confirmed",
                    outcomes,
                    lastInventory);

        } catch (RuntimeException e) {

            /*
             * Return anything that was already reserved.
             */
            List<OrderItem> savedItems =
                    orderItemRepository
                            .findByOrderId(
                                    order.getOrderId());

            for (OrderItem savedItem
                    : savedItems) {

                inventoryService.restock(
                        savedItem.getProductId(),
                        savedItem.getQuantity());
            }

            throw e;
        }
    }

    @Override
    @Transactional
    public Order cancelOrder(Long orderId) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order not found"));

        if ("CANCELLED".equals(
                order.getStatus())) {

            throw new RuntimeException(
                    "Order is already CANCELLED");
        }

        if (!"CONFIRMED".equals(
                order.getStatus())) {

            throw new RuntimeException(
                    "Only confirmed orders can be cancelled");
        }

        List<OrderItem> items =
                orderItemRepository
                        .findByOrderId(orderId);

        for (OrderItem item : items) {

            inventoryService.restock(
                    item.getProductId(),
                    item.getQuantity());
        }

        order.setStatus("CANCELLED");

        order.setReason(
                "Order cancelled and inventory restocked");

        return orderRepository.save(order);
    }

    @Override
public List<OrderHistoryResponse> getAllOrders() {

    List<Order> orders = orderRepository.findAll();

    return orders.stream()
            .map(order -> {

                List<OrderItemRequest> items =
                        orderItemRepository
                                .findByOrderId(order.getOrderId())
                                .stream()
                                .map(item ->
                                        new OrderItemRequest(
                                                item.getProductId(),
                                                item.getQuantity()))
                                .toList();

                return new OrderHistoryResponse(
                        order.getOrderId(),
                        order.getStatus(),
                        order.getReason(),
                        order.getCreatedAt(),
                        items
                );
            })
            .toList();
}
}