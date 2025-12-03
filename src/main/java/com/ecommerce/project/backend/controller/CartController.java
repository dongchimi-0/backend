package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.domain.Member;
import com.ecommerce.project.backend.dto.CartAddRequestDto;
import com.ecommerce.project.backend.dto.CartChangeOptionDto;
import com.ecommerce.project.backend.dto.CartUpdateQuantityDto;
import com.ecommerce.project.backend.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    /** 세션에서 로그인한 사용자 ID 꺼내기 */
    private Long getLoginMemberId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new IllegalStateException("NO_SESSION");
        }

        Long memberId = (Long) session.getAttribute("loginMemberId");
        if (memberId == null) {
            throw new IllegalStateException("NO_USER");
        }

        return memberId;
    }

    /** 공통 예외 처리 */
    private ResponseEntity<?> handleAuth(Runnable runnable) {
        try {
            runnable.run();
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(e.getMessage()); // 로그인 안됨
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body("BAD_REQUEST");
        }
    }

    /** 장바구니 조회 */
    @GetMapping
    public ResponseEntity<?> getCart(HttpServletRequest request) {
        try {
            Long memberId = getLoginMemberId(request);
            return ResponseEntity.ok(cartService.getCart(memberId));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body("NO_SESSION");
        }
    }

    /** 장바구니 추가 */
    @PostMapping
    public ResponseEntity<?> addToCart(HttpServletRequest request,
                                       @RequestBody CartAddRequestDto req) {

        return handleAuth(() -> {
            Long memberId = getLoginMemberId(request);
            cartService.addToCart(memberId, req); // addToCart 호출
        });
    }

    /** 수량 변경 */
    @PutMapping("/quantity")
    public ResponseEntity<?> updateQuantity(HttpServletRequest request,
                                            @RequestBody CartUpdateQuantityDto req) {

        return handleAuth(() -> {
            Long memberId = getLoginMemberId(request);
            cartService.updateQuantity(memberId, req.getCartId(), req.getQuantity()); // updateQuantity 호출
        });
    }

    /** 옵션 변경 */
    @PutMapping("/option")
    public ResponseEntity<?> changeOption(HttpServletRequest request,
                                          @RequestBody CartChangeOptionDto req) {

        return handleAuth(() -> {
            Long memberId = getLoginMemberId(request);
            cartService.changeOption(memberId, req.getCartId(), req.getNewOptionValue()); // changeOption 호출
        });
    }

    /** 장바구니 삭제 */
    @DeleteMapping("/{cartId}")
    public ResponseEntity<?> delete(HttpServletRequest request,
                                    @PathVariable Long cartId) {

        return handleAuth(() -> {
            Long memberId = getLoginMemberId(request);
            cartService.delete(cartId, memberId); // delete 호출
        });
    }

    @DeleteMapping("")
    public ResponseEntity<?> clearCart(HttpSession session) {

        Long memberId = (Long) session.getAttribute("loginMemberId");

        if (memberId == null) {
            // 비회원은 세션ID로 삭제
            cartService.clearCartBySessionId(session.getId());
        } else {
            cartService.clearCartByMemberId(memberId);
        }

        return ResponseEntity.ok().body("장바구니 전체 삭제 완료");
    }


}
