package com.ecommerce.project.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    private Long orderItemId;     // 주문 상품 항목 ID
    private Long orderId;         // 주문 ID
    private Long productId;       // 상품 ID
    private String productName;   // 상품 이름
    private Long optionId;        // 옵션 ID (단일 상품이면 null)
    private String optionValue;   // 옵션 값 (예: 블랙, L 등)
    private Integer quantity;     // 수량
    private BigDecimal price;     // 단가
    private BigDecimal subtotal;  // 합계 (price × quantity)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * ✅ Entity → DTO 변환 메서드
     */
    public static OrderItemDto fromEntity(com.ecommerce.project.backend.domain.OrderItem e) {
        return OrderItemDto.builder()
                .orderItemId(e.getOrderItemId())
                .orderId(e.getOrder().getOrderId())
                .productId(e.getProduct().getProductId())
                .productName(e.getProduct().getProductName())
                .optionId(e.getOption() != null ? e.getOption().getOptionId() : null)
                .optionValue(e.getOption() != null ? e.getOption().getOptionValue() : null)
                .quantity(e.getQuantity())
                .price(e.getPrice())
                .subtotal(e.getSubtotal())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
