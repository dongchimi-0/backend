package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.domain.Category;
import com.ecommerce.project.backend.dto.CategoryTreeResponseDto;
import com.ecommerce.project.backend.dto.CategoryDto;
import com.ecommerce.project.backend.dto.MainCategoryDto;
import com.ecommerce.project.backend.dto.SubCategoryDto;
import com.ecommerce.project.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 전체 트리 반환
    public CategoryTreeResponseDto getCategoryTree() {

        List<Category> categories = categoryRepository.findAll();

        Map<String, CategoryDto> tree = new LinkedHashMap<>();

        for (Category c : categories) {
            String code = c.getCategoryCode();
            String name = c.getCategoryTitle();

            if (code.length() == 4) {
                // 대분류
                tree.put(code, CategoryDto.builder()
                        .title(name)
                        .children(new LinkedHashMap<>())
                        .build());
            }

            else if (code.length() == 8) {
                // 중분류
                String mainCode = code.substring(0, 4);
                CategoryDto main = tree.get(mainCode);

                if (main != null) {
                    main.getChildren().put(code, SubCategoryDto.builder()
                            .title(name)
                            .children(new LinkedHashMap<>())
                            .build());
                }
            }

            else if (code.length() == 12) {
                // 소분류
                String mainCode = code.substring(0, 4);
                String subCode = code.substring(0, 8);

                CategoryDto main = tree.get(mainCode);
                if (main != null && main.getChildren().containsKey(subCode)) {
                    main.getChildren()
                            .get(subCode)
                            .getChildren()
                            .put(code, name);
                }
            }
        }

        return CategoryTreeResponseDto.builder()
                .tree(tree)
                .build();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }


    /** ✔ 단일 카테고리 조회 추가 (Controller 사용 중) */
    public Category getCategoryByCode(String code) {
        return categoryRepository.findByCategoryCode(code)
                .orElseThrow(() -> new IllegalArgumentException("카테고리 없음: " + code));
    }

    public List<MainCategoryDto> getMainCategories() {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getCategoryCode().length() == 4)
                .map(c -> new MainCategoryDto(c.getCategoryCode(), c.getCategoryTitle()))
                .toList();
    }


}
