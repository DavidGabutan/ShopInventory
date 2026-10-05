package edu.cit.gabutan.channel;

import edu.cit.gabutan.inventory.Inventory;
import edu.cit.gabutan.inventory.InventoryService;
import edu.cit.gabutan.shop.OrderService;
import edu.cit.gabutan.shop.dto.OrderItemRequest;
import edu.cit.gabutan.shop.dto.OrderRequest;
import edu.cit.gabutan.shop.dto.OrderResponse;
import edu.cit.gabutan.supplier.SupplierGateway;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
class TianggeOrderProcessor {

    private final TianggeClient client;
    private final TianggeFeedStateRepository feedStateRepository;
    private final TianggeEventRepository eventRepository;
    private final TianggeOrderRepository orderRepository;
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final SupplierGateway supplierGateway;

    TianggeOrderProcessor(
            TianggeChannelImpl channel,
            TianggeFeedStateRepository feedStateRepository,
            TianggeEventRepository eventRepository,
            TianggeOrderRepository orderRepository,
            OrderService orderService,
            InventoryService inventoryService,
            SupplierGateway supplierGateway,
            TianggeStockCoordinator stockCoordinator) {

        this.client = channel.getClient();
        this.feedStateRepository = feedStateRepository;
        this.eventRepository = eventRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.inventoryService = inventoryService;
        this.supplierGateway = supplierGateway;
        this.stockCoordinator = stockCoordinator;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 3000)
    void pollFeed() {

        TianggeFeedState state =
                feedStateRepository.findById((short) 1)
                        .orElseGet(() -> {
                            TianggeFeedState newState =
                                    new TianggeFeedState();
                            newState.setId((short) 1);
                            newState.setCursorValue(null);
                            newState.setUpdatedAt(LocalDateTime.now());
                            return feedStateRepository.save(newState);
                        });

        Long cursor = null;

        if (state.getCursorValue() != null
                && !state.getCursorValue().isBlank()) {
            cursor = Long.valueOf(state.getCursorValue());
        }

        TianggeFeedResponse response =
                client.getFeed(cursor, 50);

        if (response.events() == null
                || response.events().isEmpty()) {

            saveCursor(state, response.nextCursor());
            return;
        }

        for (TianggeFeedEvent event : response.events()) {

            TianggeEvent existing =
                    eventRepository.findByEventId(event.eventId())
                            .orElse(null);

            if (existing != null
                    && "COMPLETED".equals(
                            existing.getProcessingStatus())) {
                continue;
            }

            if (existing == null) {
                existing = new TianggeEvent();
                existing.setEventId(event.eventId());
                existing.setSequenceNumber(event.seq());
                existing.setTianggeOrderId(event.orderId());
                existing.setEventType(event.type());
                existing.setProcessingStatus("PROCESSING");
                existing.setCreatedAt(LocalDateTime.now());
                existing.setUpdatedAt(LocalDateTime.now());

                eventRepository.save(existing);
            }

            processEvent(event, existing);

            existing.setProcessingStatus("COMPLETED");
            existing.setUpdatedAt(LocalDateTime.now());
            eventRepository.save(existing);
        }

        saveCursor(state, response.nextCursor());
    }

    private void processEvent(
            TianggeFeedEvent event,
            TianggeEvent storedEvent) {

        if ("ORDER_PLACED".equals(event.type())) {
            processOrderPlaced(event, storedEvent);
            return;
        }

        if ("ORDER_CANCELLED".equals(event.type())) {
            processOrderCancelled(event, storedEvent);
            return;
        }

        throw new IllegalStateException(
                "Unsupported Tiangge event type: " + event.type());
    }

    private void processOrderPlaced(
            TianggeFeedEvent event,
            TianggeEvent storedEvent) {

        TianggeOrder existing =
                orderRepository
                        .findByTianggeOrderId(event.orderId())
                        .orElse(null);

        if (existing != null
                && existing.getShopOrderId() != null) {

            if (existing.getDecision() != null) {
                client.decideOrder(
                        event.orderId(),
                        existing.getDecision(),
                        existing.getShopOrderId(),
                        "Already processed"
                );
            }

            storedEvent.setShopOrderId(
                    existing.getShopOrderId());

            return;
        }

        List<OrderItemRequest> items =
                new ArrayList<>();

        boolean hasShortage = false;
        boolean allShortagesHaveOpenPo = true;

        for (TianggeFeedLine line : event.lines()) {

            String productId = line.sellerSku();
            int quantity = line.qty();

            items.add(
                    new OrderItemRequest(
                            productId,
                            quantity));

            Inventory inventory =
                    inventoryService.getItem(productId);

            if (quantity > inventory.getStock()) {

                hasShortage = true;

                if (!supplierGateway.hasOpenOrder(productId)) {
                    allShortagesHaveOpenPo = false;
                }
            }
        }

        OrderRequest request = new OrderRequest();
        request.setItems(items);

        OrderResponse response;

        stockCoordinator.startDeferring();
        
        try {
        
            if (hasShortage && allShortagesHaveOpenPo) {
        
                response =
                        orderService.placeBackorderedOrder(
                                request,
                                "Insufficient stock; supplier order is on the way"
                        );
        
            } else {
        
                response =
                        orderService.placeOrder(request);
            }
        
        } catch (RuntimeException e) {
        
            stockCoordinator.discardDeferred();
            throw e;
        }

        String decision;

        if ("CONFIRMED".equals(response.getStatus())) {
            decision = "ACCEPTED";
        } else if ("BACKORDERED".equals(response.getStatus())) {
            decision = "BACKORDERED";
        } else {
            decision = "REJECTED";
        }

        TianggeOrder tianggeOrder =
                new TianggeOrder();

        tianggeOrder.setTianggeOrderId(
                event.orderId());

        tianggeOrder.setShopOrderId(
                response.getOrderId());

        tianggeOrder.setDecision(
                decision);

        tianggeOrder.setCreatedAt(
                LocalDateTime.now());

        tianggeOrder.setUpdatedAt(
                LocalDateTime.now());

        orderRepository.save(tianggeOrder);

        storedEvent.setShopOrderId(
                response.getOrderId());

                try {

                    client.decideOrder(
                            event.orderId(),
                            decision,
                            response.getOrderId(),
                            response.getReason()
                    );
                
                    stockCoordinator.publishDeferred();
                
                } catch (RuntimeException e) {
                
                    stockCoordinator.discardDeferred();
                    throw e;
                }
    }

    private void processOrderCancelled(
            TianggeFeedEvent event,
            TianggeEvent storedEvent) {

        TianggeOrder tianggeOrder =
                orderRepository
                        .findByTianggeOrderId(event.orderId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No local order mapping for Tiangge order "
                                                + event.orderId()));

        if (tianggeOrder.getShopOrderId() == null) {
            throw new IllegalStateException(
                    "Tiangge order has no shop order ID: "
                            + event.orderId());
        }

        try {

            orderService.cancelOrder(
                    tianggeOrder.getShopOrderId());

        } catch (RuntimeException e) {

            if (!"Order is already CANCELLED"
                    .equals(e.getMessage())) {

                throw e;
            }
        }

        client.confirmCancellation(
                event.orderId());

        tianggeOrder.setDecision("CANCELLED");
        tianggeOrder.setUpdatedAt(
                LocalDateTime.now());

        orderRepository.save(tianggeOrder);

        storedEvent.setShopOrderId(
                tianggeOrder.getShopOrderId());
    }

    private void saveCursor(
            TianggeFeedState state,
            Long nextCursor) {

        if (nextCursor == null) {
            return;
        }

        state.setCursorValue(
                String.valueOf(nextCursor));

        state.setUpdatedAt(
                LocalDateTime.now());

        feedStateRepository.save(state);
    }
    private final TianggeStockCoordinator stockCoordinator;
}