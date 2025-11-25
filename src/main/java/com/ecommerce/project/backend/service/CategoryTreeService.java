package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.dto.CategoryDto;
import com.ecommerce.project.backend.dto.CategoryTreeResponseDto;
import com.ecommerce.project.backend.dto.SubCategoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryTreeService {

    private final CategoryService categoryService;

    public CategoryTreeResponseDto getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    public String getCategoryPath(String code) {

        var tree = categoryService.getCategoryTree().getTree(); // 전체 트리 DTO만 꺼내기

        for (var mainEntry : tree.entrySet()) {

            String mainCode = mainEntry.getKey();
            CategoryDto main = mainEntry.getValue();

            if (code.startsWith(mainCode)) {

                for (var subEntry : main.getChildren().entrySet()) {

                    String subCode = subEntry.getKey();
                    SubCategoryDto sub = subEntry.getValue();

                    if (code.startsWith(subCode)) {

                        if (sub.getChildren().containsKey(code)) {
                            return main.getTitle()
                                    + " > " + sub.getTitle()
                                    + " > " + sub.getChildren().get(code);
                        }
                    }
                }
            }
        }

        return null;
    }
}
