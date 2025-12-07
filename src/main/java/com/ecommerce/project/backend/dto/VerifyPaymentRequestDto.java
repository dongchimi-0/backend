package com.ecommerce.project.backend.dto;
import lombok.Data;
import java.util.List;

@Data
public class VerifyPaymentRequestDto {
    private String paymentId;
    private Long orderId;

    private Long addressId;
    private List<OrderRequestDTO.Item> items;
}
