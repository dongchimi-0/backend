package com.ecommerce.project.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductListDto {

    private Long productId;
    private String productName;
    private BigDecimal sellPrice;
    private BigDecimal consumerPrice;
    private String mainImg;

    public ProductListDto(
            Long productId,
            String productName,
            BigDecimal sellPrice,
            BigDecimal consumerPrice,
            String mainImg
    ) {
        this.productId = productId;
        this.productName = productName;
        this.sellPrice = sellPrice;
        this.consumerPrice = consumerPrice;

        // ⭐ 자동 이미지 최적화: w=1200 → w=300
        if (mainImg != null) {
            this.mainImg = mainImg.replace("w=1200", "w=300");
        } else {
            this.mainImg = null;
        }
    }
}
