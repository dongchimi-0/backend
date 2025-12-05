package com.ecommerce.project.backend.domain;

import com.ecommerce.project.backend.dto.ProductDto;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
    private BigDecimal sellPrice; // 판매가(할인가)

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
    private List<ProductOption> productOptions = new ArrayList<>(); // 상품 옵션

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

    @PrePersist
    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = Timestamp.valueOf(LocalDateTime.now()); // 현재 시간으로 업데이트
    }

    /** 옵션 상품 재고 합산 (옵션 상품일 경우 호출) */
    public void updateTotalStockFromOptions() {
        if (!isOption) return; // 단일상품이면 패스

        int totalStock = productOptions.stream() // `options`를 `productOptions`로 수정
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


    public void setStock(Integer stock) {
        this.stock = stock;
    }

    // 품절 상태 변경
    public void setProductStatus(Integer status) {

        this.productStatus = status;
    }


    // 옵션값을 기준으로 ProductOption을 찾는 메서드
    public ProductOption getOptionByValue(String optionValue) {
        return productOptions.stream()
                .filter(option -> option.getOptionValue().equals(optionValue))
                .findFirst()
                .orElse(null);  // 없으면 null 반환
    }

    public List<ProductOption> getOptions() {

        return productOptions;
    }


    // 상품 정보 수정 메서드
    public void updateProductInfo(Product product) {
        this.productName = product.getProductName();
        this.sellPrice = product.getSellPrice();
        this.consumerPrice = product.getConsumerPrice();
        this.stock = product.getStock();
    }


    // 옵션 상태 변경시 stock 갱신
    /*
    * isShow 상태가 변경될 때 호출되어,
    * 해당 옵션의 상태를 변경하고, 변경된 옵션의 stock을 기준으로
    * 상품의 총 재고(stock)를 갱신
    */
    public void updateOptionVisibilityAndStock(Long optionId, boolean isShow) {
        // 옵션을 찾음
        ProductOption option = this.productOptions.stream()
                .filter(opt -> opt.getProductOptionId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 옵션을 찾을 수 없습니다."));

        // 상태 변경
        option.setIsShow(isShow);

        // 옵션의 재고가 변경되었을 때 총 재고 업데이트
        updateTotalStockFromOptions();
    }

    public List<ProductImage> getSubImages() {
        return this.images; // 연결된 이미지 리스트 반환
    }

    // Product 생성자에서 ProductDto를 받도록 수정
    public Product(ProductDto dto) {
        this.productName = dto.getProductName();
        this.description = dto.getDescription();
        this.consumerPrice = dto.getConsumerPrice();
        this.sellPrice = dto.getSellPrice();
        this.stock = dto.getStock();
        this.isOption = dto.getIsOption();
        this.mainImg = dto.getMainImg();
        this.productStatus = dto.getProductStatus();
        this.isShow = dto.getIsShow();
    }

    // ProductDto에서 정보를 받아와 업데이트하는 메서드
    public void updateProductInfo(ProductDto productDto) {
        this.productName = productDto.getProductName();
        this.description = productDto.getDescription();
        this.consumerPrice = productDto.getConsumerPrice();
        this.sellPrice = productDto.getSellPrice();
        this.stock = productDto.getStock();
        this.isOption = productDto.getIsOption();
        this.mainImg = productDto.getMainImg();
        this.productStatus = productDto.getProductStatus();
        this.isShow = productDto.getIsShow();
    }



}
