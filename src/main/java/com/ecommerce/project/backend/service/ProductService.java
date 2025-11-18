package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.config.MusinsaConfig;
import com.ecommerce.project.backend.domain.Product;
import com.ecommerce.project.backend.dto.ProductDto;
import com.ecommerce.project.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final MusinsaConfig musinsaConfig;

    // musinsaConfig도 생성자 주입 받기
    public ProductService(ProductRepository productRepository, MusinsaConfig musinsaConfig) {
        this.productRepository = productRepository;
        this.musinsaConfig = musinsaConfig;
    }

    /** 전체 상품 (노출 중인 상품만) */
    public List<ProductDto> getAllVisibleProducts() {
        String baseUrl = musinsaConfig.getImageBaseUrl();
        List<Product> products = productRepository.findAllVisibleProducts();

        if (products == null || products.isEmpty()) {
            System.out.println("[ProductService] 노출 중인 상품이 없습니다.");
            return List.of();
        }

        System.out.println("[ProductService] 조회된 상품 수: " + products.size());

        return products.stream()
                .map(p -> ProductDto.fromEntity(p, baseUrl))
                .collect(Collectors.toList());
    }

    /** 단일 상품 조회 */
    public ProductDto getProductById(Long id) {
        String baseUrl = musinsaConfig.getImageBaseUrl();

        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + id));
        return ProductDto.fromEntity(p, baseUrl); // baseUrl 전달
    }

    /** 상품명 검색 */
    public List<ProductDto> searchProductsByName(String keyword) {
        String baseUrl = musinsaConfig.getImageBaseUrl();

        return productRepository.findByProductNameContaining(keyword)
                .stream()
                .map(p -> ProductDto.fromEntity(p, baseUrl))
                .collect(Collectors.toList());
    }

    /** 카테고리 코드별 조회 */
    public List<ProductDto> getProductsByCategoryCode(String categoryCode) {
        String baseUrl = musinsaConfig.getImageBaseUrl();

        return productRepository.findByCategoryCode(categoryCode)
                .stream()
                .map(p -> ProductDto.fromEntity(p, baseUrl))
                .collect(Collectors.toList());
    }



}
