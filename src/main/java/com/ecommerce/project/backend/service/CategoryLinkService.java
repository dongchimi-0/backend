package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.domain.CategoryLink;
import com.ecommerce.project.backend.repository.CategoryLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryLinkService {

    private final CategoryLinkRepository categoryLinkRepository;

    // 특정 상품의 카테고리 목록 조회
    public List<CategoryLink> getCategoriesByProductId(Long productId) {
        return categoryLinkRepository.findByProduct_ProductId(productId);
    }

    // 특정 카테고리 코드에 속한 상품 목록 조회
    public List<CategoryLink> getProductsByCategoryCode(String categoryCode) {
        return categoryLinkRepository.findByCategoryCode(categoryCode);
    }

    // 상품-카테고리 링크 저장
    public CategoryLink save(CategoryLink link) {
        return categoryLinkRepository.save(link);
    }
}
