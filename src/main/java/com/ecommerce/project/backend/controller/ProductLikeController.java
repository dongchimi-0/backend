package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.domain.Member;
import com.ecommerce.project.backend.dto.ProductDto;
import com.ecommerce.project.backend.service.ProductLikeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/like")
public class ProductLikeController {

    private final ProductLikeService likeService;

    @PostMapping("/toggle/{productId}")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long productId,
            HttpSession session) {

        Member loginMember = (Member) session.getAttribute("loginMember");

        if (loginMember == null || loginMember.getId() == null) {
            return ResponseEntity.status(401).body("로그인 필요");
        }

        Long memberId = loginMember.getId();
        boolean liked = likeService.toggleLike(memberId, productId);

        return ResponseEntity.ok(liked);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyLikes(HttpSession session) {

        Member loginMember = (Member) session.getAttribute("loginMember");

        if (loginMember == null || loginMember.getId() == null) {
            return ResponseEntity.status(401).body("로그인 필요");
        }

        List<ProductDto> wishlist = likeService.getMyLikeProducts(loginMember.getId());
        return ResponseEntity.ok(wishlist);
    }
}
