package edu.cit.gabutan.shop;

import edu.cit.gabutan.shop.dto.OrderRequest;
import edu.cit.gabutan.shop.dto.OrderResponse;

public interface OrderService {

    OrderResponse placeOrder(OrderRequest request);
}