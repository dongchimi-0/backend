package com.ecommerce.project.backend.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName; // 상품명

    @Column(columnDefinition = "TEXT")
    private String description; // 상품 설명

    @Column(name = "consumer_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal consumerPrice; // 소비자가 (정가)

    @Column(name = "sell_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellPrice; // 판매가

    @Column(nullable = false)
    private Integer stock; // 총 재고

    @Column(name = "is_option", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isOption; // 옵션 여부 (1=옵션상품, 0=단일상품)

    @Column(name = "main_img", nullable = false, length = 255)
    private String mainImg; // 대표 이미지 경로

    @Column(name = "product_status", nullable = false)
    private Integer productStatus; // 상품 상태

    @Column(name = "is_show", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isShow; // 노출 여부

    @Column(name = "created_at", insertable = false, updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "updated_at", insertable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp updatedAt;

    // product_image (1:N 매핑)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("sortOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    // 옵션 상품 (1:N 매핑)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductOption> options = new ArrayList<>();

    //CategoryLink
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CategoryLink> categoryLinks = new ArrayList<>();

    // -------------------------------------------------------
    // 재고 로직
    // -------------------------------------------------------

    /** 단일 상품 재고 차감 */
    public void decreaseStock(int quantity) {
        if (isOption) {
            throw new IllegalStateException("옵션 상품은 옵션 단위로 재고를 차감해야 합니다.");
        }
        if (this.stock - quantity < 0) {
            throw new IllegalArgumentException("재고가 부족합니다. (현재 재고: " + this.stock + ")");
        }
        this.stock -= quantity;
    }

    /** 단일 상품 재고 수정 (관리자용) */
    public void updateStock(int newStock) {
        if (isOption) {
            throw new IllegalStateException("옵션 상품은 옵션별로 재고를 수정해야 합니다.");
        }
        if (newStock < 0) {
            throw new IllegalArgumentException("재고는 음수가 될 수 없습니다.");
        }
        this.stock = newStock;
    }

//    /** 옵션 상품 재고 합산 (옵션 상품일 경우 호출) */
    public void updateTotalStockFromOptions() {
        if (!isOption) return; // 단일상품이면 패스

        int totalStock = options.stream()
                .mapToInt(ProductOption::getStock)
                .sum();

        this.stock = totalStock;
    }

    /** 판매가 수정 */
    public void updateSellPrice(BigDecimal newSellPrice) {
        if (newSellPrice != null && newSellPrice.compareTo(this.consumerPrice) > 0) {
            throw new IllegalArgumentException("판매가는 소비자가보다 높을 수 없습니다.");
        }
        this.sellPrice = newSellPrice;
    }

    //    상품 상세 설명
    public void updateDescription(String description) {

        this.description = description;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    // 품절 상태 변경
    public void setProductStatus(Integer status) {
        this.productStatus = status;
    }

}

