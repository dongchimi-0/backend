package com.ecommerce.project.backend.repository;

import com.ecommerce.project.backend.domain.CategoryLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryLinkRepository extends JpaRepository<CategoryLink, Long> {

    // 특정 상품에 연결된 카테고리 코드 목록 조회
    List<CategoryLink> findByProduct_ProductId(Long productId);

    // 특정 카테고리 코드로 연결된 상품 목록 조회
    List<CategoryLink> findByCategoryCode(String categoryCode);

    void deleteAllByProduct_ProductId(Long productId);
}
