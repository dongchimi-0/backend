package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.domain.Member;
import com.ecommerce.project.backend.dto.ProductDto;
import com.ecommerce.project.backend.dto.ProductDetailResponseDto;
import com.ecommerce.project.backend.service.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    /** 전체 + 카테고리별 상품 조회 */
    @GetMapping
    public List<ProductDto> getProducts(
            @RequestParam(required = false) String category
    ) {
        if (category != null && !category.isEmpty()) {
            return productService.getProductsByCategoryCode(category);
        }

        return productService.getAllVisibleProducts();
    }

    /** 기본 상품 정보 */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /** 상세 정보(카테고리 경로 + 옵션 + 이미지) */
    @GetMapping("/{id}/detail")
    public ResponseEntity<?> getProductDetail(
            @PathVariable Long id,
            HttpSession session) {

        Long memberId = (session != null)
                ? (Long) session.getAttribute("loginMemberId")
                : null;

        ProductDetailResponseDto dto = productService.getProductDetail(id, memberId);
        return ResponseEntity.ok(dto);
    }


    /** 검색 */
    @GetMapping("/search")
    public List<ProductDto> searchProducts(@RequestParam String keyword) {
        return productService.searchProductsByName(keyword);
    }

    /** 카테고리별 상품 조회 */
    @GetMapping("/category/{code}")
    public List<ProductDto> getProductsByCategory(@PathVariable String code) {
        return productService.getProductsByCategoryCode(code);
    }




}
