package com.ecommerce.project.backend.repository;

import com.ecommerce.project.backend.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 카테고리 코드로 조회
    Optional<Category> findByCategoryCode(String categoryCode);

    // 카테고리명으로 조회
    Optional<Category> findByCategoryTitle(String categoryTitle);
}
