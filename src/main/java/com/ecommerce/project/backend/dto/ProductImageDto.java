package com.ecommerce.project.backend.dto;

public class ProductImageDto {
    private String imageUrl;   // 이미지 URL
    private int sortOrder;     // 이미지 순서
    private long productId;    // 상품 ID

    // Getter, Setter 및 생성자 추가
    public ProductImageDto(String imageUrl, int sortOrder, long productId) {
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
        this.productId = productId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }
}
