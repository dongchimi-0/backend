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

    /** 로그인 유저 조회 공통 메서드 */
    private Member getLoginMember(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) throw new RuntimeException("로그인 필요");

        Member member = (Member) session.getAttribute("loginMember");

        if (member == null)
            throw new RuntimeException("로그인 필요");

        return member;
    }

    /** 장바구니 조회 */
    @GetMapping
    public ResponseEntity<?> getCart(HttpServletRequest request) {
        Member member = getLoginMember(request);
        return ResponseEntity.ok(cartService.getCart(member.getId()));
    }

    /** 장바구니 추가 */
    @PostMapping
    public ResponseEntity<?> addToCart(HttpServletRequest request,
                                       @RequestBody CartAddRequestDto req) {

        Member member = getLoginMember(request);

        cartService.addToCart(member.getId(), req);
        return ResponseEntity.ok().build();
    }

    /** 수량 변경 */
    @PutMapping("/quantity")
    public ResponseEntity<?> updateQuantity(HttpServletRequest request,
                                            @RequestBody CartUpdateQuantityDto req) {

        Member member = getLoginMember(request);

        cartService.updateQuantity(req.getCartId(), req.getQuantity());
        return ResponseEntity.ok().build();
    }

    /** 옵션 변경 */
    @PutMapping("/option")
    public ResponseEntity<?> changeOption(HttpServletRequest request,
                                          @RequestBody CartChangeOptionDto req) {

        Member member = getLoginMember(request);

        cartService.changeOption(member.getId(), req.getCartId(), req.getNewOptionId());
        return ResponseEntity.ok().build();
    }

    /** 장바구니 삭제 */
    @DeleteMapping("/{cartId}")
    public ResponseEntity<?> delete(HttpServletRequest request,
                                    @PathVariable Long cartId) {

        Member member = getLoginMember(request);

        cartService.delete(cartId, member.getId());
        return ResponseEntity.ok().build();
    }
}

