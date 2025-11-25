package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.domain.CategoryLink;
import com.ecommerce.project.backend.service.CategoryLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category-link")
@RequiredArgsConstructor
public class CategoryLinkController {

    private final CategoryLinkService categoryLinkService;

    // 특정 상품이 속한 카테고리 목록 조회
    @GetMapping("/product/{productId}")
    public List<CategoryLink> getCategoriesByProduct(@PathVariable Long productId) {
        return categoryLinkService.getCategoriesByProductId(productId);
    }

    // 특정 카테고리에 속한 상품 목록 조회
    @GetMapping("/category/{categoryCode}")
    public List<CategoryLink> getProductsByCategory(@PathVariable String categoryCode) {
        return categoryLinkService.getProductsByCategoryCode(categoryCode);
    }
}
