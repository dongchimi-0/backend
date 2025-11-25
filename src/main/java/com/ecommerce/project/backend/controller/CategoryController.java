package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.domain.Category;
import com.ecommerce.project.backend.dto.CategoryDto;
import com.ecommerce.project.backend.dto.MainCategoryDto;
import com.ecommerce.project.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 전체 카테고리 리스트
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    // 카테고리 단건 조회 (코드로)
    @GetMapping("/{categoryCode}")
    public Category getCategory(@PathVariable String categoryCode) {
        return categoryService.getCategoryByCode(categoryCode);
    }

    @GetMapping("/main")
    public List<MainCategoryDto> getMainCategories() {
        return categoryService.getMainCategories();
    }




}
