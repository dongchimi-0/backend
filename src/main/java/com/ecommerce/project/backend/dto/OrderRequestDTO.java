package com.ecommerce.project.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequestDTO {
    private Long addressId;

    private List<Item> items;

    @Getter @Setter
    public static class Item {
        private Long productId;
        private Integer quantity;
        private List<String> optionValues;  // ["색상 Ivory"], ["기본"]
    }
}

