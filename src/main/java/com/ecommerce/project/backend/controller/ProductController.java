package com.ecommerce.project.backend.controller;

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
// 배포시 주소 주의
@CrossOrigin(origins = "http://localhost:3000")
public class ProductController {

    private final ProductService productService;

    /** 전체 상품 리스트 (노출 중 상품만) */
    @GetMapping
    public List<ProductDto> getAllVisibleProducts() {
        return productService.getAllVisibleProducts();
    }

    /** 단일 상품 상세 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            ProductDto product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
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
