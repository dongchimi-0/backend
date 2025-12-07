package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.dto.VerifyPaymentRequestDto;
import com.ecommerce.project.backend.service.OrderService;
import com.ecommerce.project.backend.service.PortOneService;
import com.ecommerce.project.backend.dto.OrderRequestDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PortOneService portOneService;
    private final OrderService orderService;

    /** PortOne 결제 검증 */
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyPaymentRequestDto req) {

        try {
            // 1) PortOne 서버에 결제 정보 조회
            JsonNode info = portOneService.verifyPayment(req.getPaymentId());

            String status = info.get("status").asText();
            int paidAmount = info.get("amount").get("total").asInt();

            // 2) DB 주문 조회
            var order = orderService.getOrderEntity(req.getOrderId());
            int orderAmount = order.getTotalPrice().intValue();

            // 3) 금액 검증
            if (orderAmount != paidAmount)
                return ResponseEntity.badRequest().body("결제 금액 불일치");

            // 4) 결제 성공 여부 검증
            if (!status.equals("PAID"))
                return ResponseEntity.badRequest().body("결제가 완료되지 않음");

            // 5) 최종 결제 처리 (OrderItem 생성 + 재고 차감)
            orderService.completeCardPayment(req);

            return ResponseEntity.ok("결제 성공");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
