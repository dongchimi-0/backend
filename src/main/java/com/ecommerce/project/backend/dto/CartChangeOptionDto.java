package com.ecommerce.project.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartChangeOptionDto {
    private Long cartId;
    private Long newOptionId;
}
