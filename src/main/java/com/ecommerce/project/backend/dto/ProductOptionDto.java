package com.ecommerce.project.backend.dto;

import com.ecommerce.project.backend.domain.ProductOption;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOptionDto {
    private Long optionId;
    private Long productId;

    private String optionType;   // 옵션 타입 (C=color, S=size)
    private String optionTitle;  // 옵션 제목 (예: 색상, 사이즈)
    private String optionValue;  // 옵션 값 (예: Red, L)
    private Integer stock;       // 재고 수량
    private String colorCode;    // 색상 코드 (#FFFFFF 등)

    private BigDecimal consumerPrice;
    private BigDecimal sellPrice;
    private Boolean isShow;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;




    public static ProductOptionDto fromEntity(ProductOption e) {
        return ProductOptionDto.builder()
                .optionId(e.getOptionId())
                .productId(e.getProduct().getProductId())
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

}

