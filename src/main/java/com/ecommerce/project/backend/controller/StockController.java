package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.service.StockService;
import org.springframework.web.bind.annotation.*;

// 사용자가 상품을 구매했을 때 재고를 자동 차감하는 API

@RestController
@RequestMapping("/api/orders")

// 배포시 주소 바꾸기!!
@CrossOrigin(origins = "http://localhost:3000")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    // 주문 발생 시 재고 차감
    @PostMapping("/{productId}/decrease-stock")
    public String decreaseStock(@PathVariable Long productId, @RequestParam int quantity) {
        stockService.decreaseStock(productId, quantity);
        return "재고가 정상적으로 차감되었습니다.";
    }
}
