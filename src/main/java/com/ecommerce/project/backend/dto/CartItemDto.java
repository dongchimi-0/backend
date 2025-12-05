package com.ecommerce.project.backend.dto;

import lombok.*;

@Getter
@Setter
@Builder
public class CartItemDto {
    private Long cartId;
    private Long productId;
    private String productName;
    private String thumbnail;

    private Integer quantity;
    private Integer price;     // product.sellPrice
    private Integer stock;     // 남은 재고
    private Boolean soldOut;

    private String optionValue;  // 옵션 정보 (없으면 null)
    private String optionTitle;

}



