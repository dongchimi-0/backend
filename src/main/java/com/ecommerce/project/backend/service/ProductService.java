package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.domain.Product;
import com.ecommerce.project.backend.dto.ProductDto;
import com.ecommerce.project.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**  전체 상품 (노출 중인 상품만) */
    public List<ProductDto> getAllVisibleProducts() {
        return productRepository.findByIsShowTrue().stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }

    /** 단일 상품 조회 */
    public ProductDto getProductById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + id));
        return ProductDto.fromEntity(p);
    }

    /**  상품명 검색 */
    public List<ProductDto> searchProductsByName(String keyword) {
        return productRepository.findByProductNameContaining(keyword)
                .stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**  카테고리 코드별 조회 */
    public List<ProductDto> getProductsByCategoryCode(String categoryCode) {
        return productRepository.findByCategoryCode(categoryCode)
                .stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }
}
