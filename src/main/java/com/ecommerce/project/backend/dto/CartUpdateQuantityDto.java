package com.ecommerce.project.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartUpdateQuantityDto {
    private Long cartId;
    private Integer quantity;
}
