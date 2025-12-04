package com.ecommerce.project.backend.dto;

public class CategoryLinkDto {

    private Long productId;
    private String categoryCode;

    public CategoryLinkDto(Long productId, String categoryCode) {
        this.productId = productId;
        this.categoryCode = categoryCode;
    }

    public Long getProductId() {
        return productId;
    }
    public String getCategoryCode() {
        return categoryCode;
    }
}


