package com.ecommerce.project.backend.service;


import com.ecommerce.project.backend.domain.Product;
import com.ecommerce.project.backend.domain.ProductOption;
import com.ecommerce.project.backend.dto.ProductOptionDto;
import com.ecommerce.project.backend.mapper.ProductOptionMapper;
import com.ecommerce.project.backend.repository.ProductOptionRepository;
import com.ecommerce.project.backend.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductOptionService {

    private final ProductOptionRepository productOptionRepository;
    private final ProductRepository productRepository;

    /**
     * 옵션 등록 (비즈니스 규칙 적용)
     */
    public ProductOptionDto saveOption(ProductOptionDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        ProductOption entity = ProductOptionMapper.toEntity(dto);
        entity.setProduct(product);

        // 비즈니스 검증은 기존 그대로 유지
        if ("C".equalsIgnoreCase(entity.getOptionType()) &&
                (entity.getColorCode() == null || entity.getColorCode().isEmpty())) {
            throw new IllegalArgumentException("색상 옵션에는 colorCode가 필요합니다.");
        }

        if (entity.getConsumerPrice() == null) {
            entity.setConsumerPrice(product.getConsumerPrice());
        }

        if (entity.getSellPrice() == null) {
            entity.setSellPrice(entity.getConsumerPrice());
        }

        if (entity.getIsShow() == null) {
            entity.setIsShow(true);
        }

        ProductOption saved = productOptionRepository.save(entity);
        return ProductOptionMapper.toDto(saved);
    }


    /**
     * 특정 상품의 옵션 목록 조회
     */
    public List<ProductOption> getOptionsByProduct(Long productId) {
        return productOptionRepository.findByProduct_ProductId(productId);
    }

    /**
     *  옵션 단건 조회
     */
    public ProductOption getOption(Long optionId) {
        return productOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 옵션이 존재하지 않습니다."));
    }

    /**
     * 옵션 수정
     */
    public ProductOption updateOption(Long optionId, ProductOption newData) {
        ProductOption existing = getOption(optionId);

        existing.setOptionType(newData.getOptionType());
        existing.setOptionTitle(newData.getOptionTitle());
        existing.setOptionValue(newData.getOptionValue());
        existing.setColorCode(newData.getColorCode());
        existing.setStock(newData.getStock());
        existing.setIsShow(newData.getIsShow());
        existing.setConsumerPrice(newData.getConsumerPrice());
        existing.setSellPrice(newData.getSellPrice());

        return productOptionRepository.save(existing);
    }

    /**
     * 옵션 삭제
     */
    public void deleteOption(Long optionId) {
        productOptionRepository.deleteById(optionId);
    }
}
