package com.ecommerce.project.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CartResponseDto {
    private List<CartItemDto> items;
    private Integer totalPrice;
    private Integer totalQuantity;

}

