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

    /** 세션에서 로그인 사용자 꺼내기 (안전한 버전) */
    private Member getLoginMember(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new IllegalStateException("NO_SESSION");
        }

        Member member = (Member) session.getAttribute("loginMember");

        if (member == null) {
            throw new IllegalStateException("NO_USER");
        }

        return member;
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
            Member member = getLoginMember(request);
            return ResponseEntity.ok(cartService.getCart(member.getId()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body("NO_SESSION");
        }
    }

    /** 장바구니 추가 */
    @PostMapping
    public ResponseEntity<?> addToCart(HttpServletRequest request,
                                       @RequestBody CartAddRequestDto req) {

        return handleAuth(() -> {
            Member member = getLoginMember(request);
            cartService.addToCart(member.getId(), req);
        });
    }

    /** 수량 변경 */
    @PutMapping("/quantity")
    public ResponseEntity<?> updateQuantity(HttpServletRequest request,
                                            @RequestBody CartUpdateQuantityDto req) {

        return handleAuth(() -> {
            Member member = getLoginMember(request);
            cartService.updateQuantity(member.getId(), req.getCartId(), req.getQuantity());
        });
    }

    /** 옵션 변경 */
    @PutMapping("/option")
    public ResponseEntity<?> changeOption(HttpServletRequest request,
                                          @RequestBody CartChangeOptionDto req) {

        return handleAuth(() -> {
            Member member = getLoginMember(request);
            cartService.changeOption(member.getId(), req.getCartId(), req.getNewOptionId());
        });
    }

    /** 장바구니 삭제 */
    @DeleteMapping("/{cartId}")
    public ResponseEntity<?> delete(HttpServletRequest request,
                                    @PathVariable Long cartId) {

        return handleAuth(() -> {
            Member member = getLoginMember(request);
            cartService.delete(cartId, member.getId());
        });
    }
}

