package edu.cit.gabutan.shop;

import edu.cit.gabutan.shop.dto.OrderHistoryResponse;
import edu.cit.gabutan.shop.dto.OrderRequest;
import edu.cit.gabutan.shop.dto.OrderResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    private final OrderService orderService;

    public OrderController(
            OrderService orderService) {

        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse placeOrder(
            @RequestBody OrderRequest request) {

        return orderService.placeOrder(request);
    }

    @GetMapping
    public List<OrderHistoryResponse> getOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long orderId) {

        try {

            return ResponseEntity.ok(
                    orderService.cancelOrder(orderId));

        } catch (RuntimeException e) {

            if ("Order not found"
                    .equals(e.getMessage())) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }

            if ("Order is already CANCELLED"
                    .equals(e.getMessage())) {

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(e.getMessage());
            }

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }
}