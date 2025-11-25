package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.dto.ProductDetailResponseDto;
import com.ecommerce.project.backend.dto.ProductDto;
import com.ecommerce.project.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ProductController {

    private final ProductService productService;

    /** 전체 상품 리스트 (노출 중 상품만) */
    @GetMapping
    public List<ProductDto> getAllVisibleProducts() {
        return productService.getAllVisibleProducts();
    }

    /** 기본 정보 (리스트·관리자 페이지에서 사용) */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            ProductDto product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /** 상세 정보 (옵션 + 카테고리 포함) */
    @GetMapping("/{id}/detail")
    public ResponseEntity<ProductDetailResponseDto> getProductDetail(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductDetail(id));
    }

    /** 상품명 검색 */
    @GetMapping("/search")
    public List<ProductDto> searchProducts(@RequestParam String keyword) {
        return productService.searchProductsByName(keyword);
    }

    /** 카테고리 코드별 상품 조회 */
    @GetMapping("/category/{code}")
    public List<ProductDto> getProductsByCategory(@PathVariable("code") String code) {
        return productService.getProductsByCategoryCode(code);
    }
}
