package com.ecommerce.project.backend.mapper;

import com.ecommerce.project.backend.domain.ProductOption;
import com.ecommerce.project.backend.dto.ProductOptionDto;

public class ProductOptionMapper {

    // Entity → DTO
    public static ProductOptionDto toDto(ProductOption e) {
        if (e == null) return null;

        return ProductOptionDto.builder()
                .optionId(e.getOptionId())
                .productId(e.getProduct() != null ? e.getProduct().getProductId() : null)
                .consumerPrice(e.getConsumerPrice())
                .sellPrice(e.getSellPrice())
                .isShow(e.getIsShow())
                .optionType(e.getOptionType())
                .optionTitle(e.getOptionTitle())
                .optionValue(e.getOptionValue())
                .stock(e.getStock())
                .colorCode(e.getColorCode())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    // DTO → Entity
    public static ProductOption toEntity(ProductOptionDto d) {
        if (d == null) return null;

        return ProductOption.builder()
                .optionId(d.getOptionId())
                .consumerPrice(d.getConsumerPrice())
                .sellPrice(d.getSellPrice())
                .isShow(d.getIsShow())
                .optionType(d.getOptionType())
                .optionTitle(d.getOptionTitle())
                .optionValue(d.getOptionValue())
                .stock(d.getStock())
                .colorCode(d.getColorCode())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
