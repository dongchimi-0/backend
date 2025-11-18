package com.ecommerce.project.backend.repository;

import com.ecommerce.project.backend.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductProductIdOrderBySortOrderAsc(Long productId);
}

