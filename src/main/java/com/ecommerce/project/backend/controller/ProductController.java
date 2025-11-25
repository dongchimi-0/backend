package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.dto.ProductDto;
import com.ecommerce.project.backend.dto.ProductDetailResponseDto;
import com.ecommerce.project.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ProductController {

    private final ProductService productService;

    /** 전체 상품 리스트 */
    @GetMapping
    public List<ProductDto> getAllVisibleProducts() {
        return productService.getAllVisibleProducts();
    }

    /** 기본 상품 정보 */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /** 상세 정보(카테고리 경로 + 옵션 + 이미지) */
    @GetMapping("/{id}/detail")
    public ResponseEntity<?> getProductDetail(@PathVariable Long id) {
        ProductDetailResponseDto dto = productService.getProductDetail(id);
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
