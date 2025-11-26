package com.ecommerce.project.backend.dto;

import lombok.Getter;
import lombok.Setter;

//@Data
@Getter
@Setter
public class CartAddRequestDto {
    private Long productId;   // 상품 ID (필수)
    private Long optionId;    // 옵션상품이면 옵션ID, 단일상품이면 null
    private Integer quantity; // 수량

}
