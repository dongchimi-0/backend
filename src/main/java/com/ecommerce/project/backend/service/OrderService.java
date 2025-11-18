//package com.ecommerce.project.backend.service;
//
//import com.ecommerce.project.backend.domain.*;
//
//import com.ecommerce.project.backend.dto.*;
//import com.ecommerce.project.backend.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.math.BigDecimal;
//import java.util.*;
//import java.util.stream.Collectors;
package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.domain.*;
import com.ecommerce.project.backend.dto.OrderDto;
import com.ecommerce.project.backend.dto.OrderItemDto;
import com.ecommerce.project.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StockService stockService;

    public OrderDto createOrder(Long memberId) {
        List<Cart> cartItems = cartRepository.findByMember_Id(memberId);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어 있습니다.");
        }

        BigDecimal totalPrice = cartItems.stream()
                .map(c -> c.getPrice().multiply(BigDecimal.valueOf(c.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .member(cartItems.get(0).getMember())
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8))
                .totalPrice(totalPrice)
                .status("ORDERED")
                .paymentMethod("CARD")
                .build();
        orderRepository.save(order);

        List<OrderItemDto> items = new ArrayList<>();
        for (Cart c : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(c.getProduct())
                    .option(c.getProductOption())
                    .quantity(c.getQuantity())
                    .price(c.getPrice())
                    .build();
            orderItemRepository.save(orderItem);

            // 재고 차감
            stockService.decreaseStock(c.getProduct().getProductId(), c.getQuantity());

            items.add(OrderItemDto.builder()
                    .productName(c.getProduct().getProductName())
                    .quantity(c.getQuantity())
                    .price(c.getPrice())
                    .subtotal(c.getPrice().multiply(BigDecimal.valueOf(c.getQuantity())))
                    .build());
        }

        cartRepository.deleteAll(cartItems);

        return OrderDto.builder()
                .orderNumber(order.getOrderNumber())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .items(items)
                .build();
    }

    public List<OrderDto> getOrdersByMember(Long memberId) {
        List<Order> orders = orderRepository.findByMember_Id(memberId);
        return orders.stream()
                .map(o -> OrderDto.builder()
                        .orderNumber(o.getOrderNumber())
                        .totalPrice(o.getTotalPrice())
                        .status(o.getStatus())
                        .paymentMethod(o.getPaymentMethod())
                        .build())
                .collect(Collectors.toList());
    }
}

