package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.dto.OrderDto;
import com.ecommerce.project.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create/{memberId}")
    public ResponseEntity<OrderDto> createOrder(@PathVariable Long memberId) {
        OrderDto order = orderService.checkout(memberId);
        return ResponseEntity.ok(order);
    }
}

