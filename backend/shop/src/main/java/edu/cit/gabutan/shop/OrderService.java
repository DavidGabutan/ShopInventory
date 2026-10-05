package edu.cit.gabutan.shop;

import edu.cit.gabutan.shop.dto.OrderHistoryResponse;
import edu.cit.gabutan.shop.dto.OrderRequest;
import edu.cit.gabutan.shop.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(OrderRequest request);

    OrderResponse placeBackorderedOrder(
            OrderRequest request,
            String reason
    );

    Order cancelOrder(Long orderId);

    List<OrderHistoryResponse> getAllOrders();
}