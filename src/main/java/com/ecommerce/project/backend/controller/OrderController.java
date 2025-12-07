package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.domain.Member;
import com.ecommerce.project.backend.dto.OrderDto;
import com.ecommerce.project.backend.dto.OrderRequestDTO;
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
            @RequestBody OrderRequestDTO req,
            HttpSession session
    ) {
        Long memberId = (Long) session.getAttribute("loginMemberId");
        if (memberId == null)
            return ResponseEntity.status(401).body("NOT_LOGIN");

        OrderDto order = orderService.checkout(memberId, req);

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

    /** 카드 결제 주문 생성, 결제창 띄우기 전에 Order 생성 (status=READY) */
    @PostMapping("/checkout/card")
    public ResponseEntity<?> checkoutForCard(
            @RequestBody OrderRequestDTO req,
            HttpSession session
    ) {
        Long memberId = (Long) session.getAttribute("loginMemberId");
        if (memberId == null) {
            return ResponseEntity.status(401).body("NOT_LOGIN");
        }

        return ResponseEntity.ok(orderService.checkoutForCard(memberId, req));
    }
}
