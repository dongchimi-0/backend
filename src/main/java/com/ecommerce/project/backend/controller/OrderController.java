package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.domain.Member;
import com.ecommerce.project.backend.dto.OrderDto;
import com.ecommerce.project.backend.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(
            @RequestBody Map<String, Object> data,
            HttpServletRequest request
    ) {

        HttpSession session = request.getSession(false);
        if (session == null) return ResponseEntity.status(401).build();

        Long memberId = (Long) session.getAttribute("loginMemberId");
        if (memberId == null) return ResponseEntity.status(401).body("NOT_LOGIN");

        Long addressId = Long.valueOf(data.get("addressId").toString());

        OrderDto order = orderService.checkout(memberId, addressId);

        return ResponseEntity.ok(order);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getOrderHistory(HttpSession session) {

        Long memberId = (Long) session.getAttribute("loginMemberId");
        if (memberId == null) {
            return ResponseEntity.status(401).body("NOT_LOGIN");
        }

        return ResponseEntity.ok(orderService.getOrderHistory(memberId));
    }

}
