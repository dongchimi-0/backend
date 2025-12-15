package com.ecommerce.project.backend.domain;

import com.ecommerce.project.backend.dto.ProductImageDto;
import com.ecommerce.project.backend.repository.ProductRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_image")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @Column(nullable = false, length = 255)
    private String imageUrl;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;


    public ProductImage(String imageUrl, Product product) {
        this.imageUrl = imageUrl;
        this.product = product;
    }

    // ProductImageDto에서 값을 받아오는 생성자
    public ProductImage(String imageUrl, int sortOrder, Product product) {
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
        this.product = product;  // product는 setter 없이 생성자를 통해 설정
    }

    // ProductImageDto를 ProductImage로 변환하는 메서드
    public static ProductImage fromDto(ProductImageDto imageDto, ProductRepository productRepository) {
        // productId로 Product 객체를 가져옵니다.
        Product product = productRepository.findById(imageDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + imageDto.getProductId()));

        // ProductImage 객체를 생성하고 반환
        return new ProductImage(imageDto.getImageUrl(), imageDto.getSortOrder(), product);
    }

    public long getProductId() {
        return product.getProductId();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;  // Product와 연결
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;  // imageUrl 수정용 setter
    }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
