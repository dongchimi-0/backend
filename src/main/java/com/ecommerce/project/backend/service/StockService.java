package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.domain.Product;
import com.ecommerce.project.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

//주문이 발생했을 때 자동으로 재고 차감 처리

@Service
public class StockService {
    private final ProductRepository productRepository;

    public StockService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 주문 발생 시 재고 차감
    public void decreaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        product.decreaseStock(quantity); // ← 도메인 로직 사용
        productRepository.save(product);
    }
}
