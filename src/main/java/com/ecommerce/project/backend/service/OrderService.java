//package com.ecommerce.project.backend.service;
//
//import com.ecommerce.project.backend.domain.*;
//
//import com.ecommerce.project.backend.dto.*;
//import com.ecommerce.project.backend.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.math.BigDecimal;
//import java.util.*;
//import java.util.stream.Collectors;
package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.domain.*;
import com.ecommerce.project.backend.dto.OrderDto;
import com.ecommerce.project.backend.dto.OrderItemDto;
import com.ecommerce.project.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductRepository productRepository;

    public OrderDto checkout(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        List<Cart> carts = cartRepository.findByMember_Id(memberId);
        if (carts.isEmpty()) throw new RuntimeException("장바구니 비었습니다.");

        // 1) 총 금액 계산
        int totalPrice = carts.stream()
                .mapToInt(c -> c.getProduct().getSellPrice().intValue() * c.getQuantity())
                .sum();


        /* -------------------------------------------
         * 2) 재고 체크 (추가)
         * ------------------------------------------- */
        for (Cart c : carts) {
            Product p = c.getProduct();
            ProductOption opt = c.getOption();

            if (opt != null) {  // 옵션 상품
                if (opt.getStock() < c.getQuantity()) {
                    throw new RuntimeException("옵션 재고 부족: " + opt.getOptionValue());
                }
            } else {            // 단일 상품
                if (p.getStock() < c.getQuantity()) {
                    throw new RuntimeException("상품 재고 부족: " + p.getProductName());
                }
            }
        }


        /* -------------------------------------------
         * 3) 주문 생성 (기존 코드 그대로)
         * ------------------------------------------- */
        Order order = orderRepository.save(
                Order.builder()
                        .member(member)
                        .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8))
                        .paymentMethod("POINT")
                        .status("PAID")
                        .totalPrice(BigDecimal.valueOf(totalPrice))
                        .build()
        );

        List<OrderItemDto> items = new ArrayList<>();


        /* -------------------------------------------
         * 4) 주문 상세 생성 + 재고 차감 (추가)
         * ------------------------------------------- */
        for (Cart c : carts) {

            Product p = c.getProduct();
            ProductOption opt = c.getOption();

            // 주문 상세 저장 (기존)
            orderItemRepository.save(
                    OrderItem.builder()
                            .order(order)
                            .product(p)
                            .option(opt)
                            .quantity(c.getQuantity())
                            .price(p.getSellPrice())
                            .build()
            );

            // (추가) 재고 차감
            if (opt != null) {  // 옵션 상품
                int newStock = opt.getStock() - c.getQuantity();
                opt.setStock(newStock);
                productOptionRepository.save(opt);

                // 옵션 전체 재고 합산 → product.stock 업데이트
                int mergedStock = p.getOptions().stream()
                        .mapToInt(ProductOption::getStock)
                        .sum();
                p.setStock(mergedStock);

            } else {            // 단일 상품
                p.setStock(p.getStock() - c.getQuantity());
            }

            // (추가) 품절 처리
            if (p.getStock() == 0) {
                p.setProductStatus(20); // 품절
            }

            productRepository.save(p);

            // OrderItemDto 추가
            items.add(
                    OrderItemDto.builder()
                            .productName(p.getProductName())
                            .quantity(c.getQuantity())
                            .price(p.getSellPrice())
                            .subtotal(p.getSellPrice().multiply(BigDecimal.valueOf(c.getQuantity())))
                            .build()
            );
        }


        /* -------------------------------------------
         * 5) 장바구니 삭제 (기존)
         * ------------------------------------------- */
        cartRepository.deleteAll(carts);


        /* -------------------------------------------
         * 6) OrderDto 반환 (기존)
         * ------------------------------------------- */
        return OrderDto.builder()
                .orderNumber(order.getOrderNumber())
                .totalPrice(order.getTotalPrice())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .items(items)
                .build();
    }
}



