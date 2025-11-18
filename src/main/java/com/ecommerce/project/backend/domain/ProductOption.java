package com.ecommerce.project.backend.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_option")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")  // PK 명시
    private Long optionId; // 옵션 고유 번호 (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference
    private Product product; // 어떤 상품의 옵션인지 (FK)

    @Column(name = "consumer_price", precision = 10, scale = 2)
    private BigDecimal consumerPrice; // 옵션별 소비자 가격 (없으면 상품 기본 가격 사용)

    @Column(name = "sell_price", precision = 10, scale = 2)
    private BigDecimal sellPrice; // 옵션별 판매가

    @Column(name = "is_show", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isShow; // 옵션 노출 여부 (true=보임, false=숨김)

    @Column(name = "option_type", length = 1, nullable = false)
    private String optionType; // 옵션 타입 (C=색상, N=일반)

    @Column(name = "option_title", length = 30)
    private String optionTitle; // 옵션명 (예: 색상, 사이즈)

    @Column(name = "option_value", length = 100)
    private String optionValue; // 옵션 값 (예: Red, Blue, S, M, L)

    @Column(name = "stock")
    private Integer stock; // 재고 수량

    @Column(name = "color_code", length = 20)
    private String colorCode; // 색상 코드 (#FFFFFF 등)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** 색상 옵션일 때만 colorCode 허용 */
    public boolean isColorRequired() {
        return "C".equalsIgnoreCase(optionType);
    }

    /** 옵션 소비자 가격이 없으면 상품의 기본 가격으로 대체 */
    public BigDecimal getEffectiveConsumerPrice() {
        return (consumerPrice != null)
                ? consumerPrice
                : product != null ? product.getConsumerPrice() : BigDecimal.ZERO;
    }
}
