package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.dto.CartDto;
import com.ecommerce.project.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // 필요 시 CORS 추가
public class CartController {

    private final CartService cartService;

    // 장바구니 담기
    @PostMapping
    public ResponseEntity<CartDto> addToCart(@RequestBody CartDto cartDto) {
        CartDto saved = cartService.addToCart(cartDto);
        return ResponseEntity.ok(saved);
    }

    // 회원별 장바구니 조회
    @GetMapping("/{memberId}")
    public ResponseEntity<List<CartDto>> getCartByMember(@PathVariable Long memberId) {
        List<CartDto> cartList = cartService.getCartByMember(memberId);
        return ResponseEntity.ok(cartList);
    }

    // 장바구니 항목 삭제
    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartId) {
        cartService.deleteCartItem(cartId);
        return ResponseEntity.noContent().build();
    }
}
