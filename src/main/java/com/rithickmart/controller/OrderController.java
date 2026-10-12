
package com.rithickmart.controller;

import com.rithickmart.model.*;
import com.rithickmart.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final UserRepository users;
    private final ProductRepository products;
    private final OrderRepository orders;
    private final OrderItemRepository items;

    public OrderController(
            UserRepository users,
            ProductRepository products,
            OrderRepository orders,
            OrderItemRepository items) {
        this.users = users;
        this.products = products;
        this.orders = orders;
        this.items = items;
    }

    public record ItemRequest(Long productId, int quantity) {}

    public record CheckoutRequest(List<ItemRequest> items) {}

    public record ItemResponse(
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal) {}

    public record OrderResponse(
            Long id,
            BigDecimal totalAmount,
            String paymentMethod,
            String status,
            LocalDateTime createdAt,
            List<ItemResponse> items) {}

    @PostMapping("/checkout")
    @Transactional
    public OrderResponse checkout(
            @RequestBody CheckoutRequest request,
            Authentication authentication) {

        if (request == null || request.items() == null
                || request.items().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Your cart is empty");
        }

        User user = users.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Please log in"));

        Order order = new Order();
        order.setUser(user);
        order.setPaymentMethod("COD");
        order.setStatus("PLACED");
        order.setTotalAmount(BigDecimal.ZERO);

        order = orders.saveAndFlush(order);

        BigDecimal total = BigDecimal.ZERO;

        for (ItemRequest requested : request.items()) {

            if (requested == null || requested.productId() == null
                    || requested.quantity() < 1
                    || requested.quantity() > 100) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid product quantity");
            }

            Product product = products.findByIdForUpdate(
                    requested.productId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Product not found"));

            int quantity = requested.quantity();

            if (product.getStock() < quantity) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Insufficient stock for " + product.getName());
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(quantity));

            product.setStock(product.getStock() - quantity);
            products.save(product);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            items.save(item);

            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
        orders.save(order);

        return getOrderResponse(order);
    }

    @GetMapping("/my")
    @Transactional(readOnly = true)
    public List<OrderResponse> myOrders(Authentication authentication) {

        User user = users.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Please log in"));

        return orders.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::getOrderResponse)
                .toList();
    }

    private OrderResponse getOrderResponse(Order order) {

        List<ItemResponse> orderItems = items.findByOrder(order)
                .stream()
                .map(item -> new ItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(
                                BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                order.getStatus(),
                order.getCreatedAt(),
                orderItems);
    }
}
