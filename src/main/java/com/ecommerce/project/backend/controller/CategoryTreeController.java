package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.dto.CategoryTreeResponseDto;
import com.ecommerce.project.backend.service.CategoryTreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryTreeController {

    private final CategoryTreeService categoryTreeService;

    @GetMapping("/tree")
    public CategoryTreeResponseDto getCategoryTree() {
        return categoryTreeService.getCategoryTree();
    }
}
