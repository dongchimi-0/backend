package com.ecommerce.project.backend.dto;

import com.ecommerce.project.backend.domain.Cart;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {
    private Long cartId;
    private Long memberId;
    private Long productId;
    private Long optionId;
    private Integer quantity;
    private BigDecimal price;
    private Boolean isSelected;

    public static CartDto fromEntity(Cart cart) {
        return CartDto.builder()
                .cartId(cart.getCartId())
                .memberId(cart.getMember().getId())
                .productId(cart.getProduct().getProductId())
                .optionId(cart.getProductOption() != null ? cart.getProductOption().getOptionId() : null)
                .quantity(cart.getQuantity())
                .price(cart.getPrice())
                .isSelected(cart.getIsSelected())
                .build();
    }
}


