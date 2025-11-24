//package com.ecommerce.project.backend.controller;
//
//import com.ecommerce.project.backend.service.ProductLikeService;
//import jakarta.servlet.http.HttpSession;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/like")
//public class ProductLikeController {
//
//    private final ProductLikeService likeService;
//
//    @PostMapping("/toggle/{productId}")
//    public ResponseEntity<?> toggleLike(
//            @PathVariable Long productId,
//            HttpSession session) {
//
//        Long memberId = (Long) session.getAttribute("memberId");
//        if (memberId == null) {
//            return ResponseEntity.status(401).body("로그인 필요");
//        }
//
//        boolean liked = likeService.toggleLike(memberId, productId);
//        return ResponseEntity.ok(liked);
//    }
//
//    @GetMapping("/my")
//    public ResponseEntity<?> getMyLikes(HttpSession session) {
//
//        Long memberId = (Long) session.getAttribute("memberId");
//        if (memberId == null) {
//            return ResponseEntity.status(401).body("로그인 필요");
//        }
//
//        return ResponseEntity.ok(likeService.getMyLikes(memberId));
//    }
//}


package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.service.ProductLikeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/like")
public class ProductLikeController {

    private final ProductLikeService likeService;

    @PostMapping("/toggle/{productId}")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long productId,
            HttpSession session) {

        Long memberId = (Long) session.getAttribute("memberId");

        if (memberId == null) {
            return ResponseEntity.status(401).body("로그인 필요");
        }

        boolean liked = likeService.toggleLike(memberId, productId);
        return ResponseEntity.ok(liked);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyLikes(HttpSession session) {

        Long memberId = (Long) session.getAttribute("memberId");

        if (memberId == null) {
            return ResponseEntity.status(401).body("로그인 필요");
        }

        return ResponseEntity.ok(likeService.getMyLikes(memberId));
    }
}

