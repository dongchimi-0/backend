package com.ecommerce.project.backend.dto;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class OrderDto {
    private String orderNumber;
    private BigDecimal totalPrice;
    private String status;
    private String paymentMethod;
    private List<OrderItemDto> items;
}

