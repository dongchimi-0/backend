package com.ecommerce.project.backend.dto;

import com.ecommerce.project.backend.domain.Product;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    private Long productId;
    private String productName;
    private String description;
    private BigDecimal consumerPrice;
    private BigDecimal sellPrice;
    private Integer stock;
    private Boolean isOption;
    private String mainImg;
    private Integer productStatus;
    private Boolean isShow;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public static ProductDto fromEntity(Product p) {
        return ProductDto.builder()
                .productId(p.getProductId())
                .productName(p.getProductName())
                .description(p.getDescription())
                .consumerPrice(p.getConsumerPrice())
                .sellPrice(p.getSellPrice())
                .stock(p.getStock())
                .isOption(p.getIsOption())
                .mainImg(p.getMainImg())
                .productStatus(p.getProductStatus())
                .isShow(p.getIsShow())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
