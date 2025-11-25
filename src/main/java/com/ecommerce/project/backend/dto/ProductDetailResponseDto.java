package com.ecommerce.project.backend.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ProductDetailResponseDto {

    private Long productId;
    private String productName;
    private String description;

    private BigDecimal consumerPrice;
    private BigDecimal sellPrice;

    private Integer stock;
    private Boolean isOption;
    private String mainImg;
    private List<String> subImages;
    private Integer productStatus;
    private Boolean isShow;

    // 카테고리 추가
    private List<String> categories;

    // 옵션
    private List<OptionDto> options;

    //TOP > 후드/맨투맨 > 오버핏 후드티"
    private String categoryPath;
}
