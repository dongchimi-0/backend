package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.config.MusinsaConfig;
import com.ecommerce.project.backend.domain.*;
import com.ecommerce.project.backend.dto.MemberAddressDto;
import com.ecommerce.project.backend.dto.OrderDto;
import com.ecommerce.project.backend.dto.OrderItemDto;
import com.ecommerce.project.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.project.backend.dto.PaymentOrderDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final MemberAddressRepository memberAddressRepository;
    private final MusinsaConfig musinsaConfig;

    @Transactional
    public OrderDto checkout(Long memberId, Long addressId) {

        String baseUrl = musinsaConfig.getImageBaseUrl();

        // -------------------------------
        // 1) 회원 & 배송지 조회
        // -------------------------------
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        MemberAddress address = memberAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("배송지 없음"));


        // -------------------------------
        // 2) 장바구니 조회
        // -------------------------------
        List<Cart> carts = cartRepository.findByMember_Id(memberId);
        if (carts.isEmpty()) throw new RuntimeException("장바구니가 비어 있습니다.");

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItemDto> itemDtos = new ArrayList<>();


        // -------------------------------
        // 3) 총 결제 금액 계산 + 재고 체크
        // -------------------------------
        for (Cart c : carts) {
            Product p = c.getProduct();
            ProductOption opt = resolveCartOption(c);
            int qty = c.getQuantity();

            BigDecimal unitPrice = (opt != null) ? opt.getSellPrice() : p.getSellPrice();

            if (opt != null && opt.getStock() < qty)
                throw new RuntimeException("옵션 재고 부족: " + opt.getOptionValue());

            if (opt == null && p.getStock() < qty)
                throw new RuntimeException("상품 재고 부족: " + p.getProductName());

            totalPrice = totalPrice.add(unitPrice.multiply(BigDecimal.valueOf(qty)));
        }


        // -------------------------------
        // 4) 회원 포인트 차감
        // -------------------------------
        if (member.getPoint() < totalPrice.intValue())
            throw new RuntimeException("포인트 부족");

        member.setPoint(member.getPoint() - totalPrice.intValue());
        memberRepository.save(member);



        // -------------------------------
        // 5) 주문 생성
        // -------------------------------
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 6);
        String orderNum = "YDJ-" + date + "-" + random;


        Order order = orderRepository.save(
                Order.builder()
                        .member(member)
                        .receiverName(address.getName())
                        .receiverPhone(address.getPhone())
                        .address(address.getAddress())
                        .addressDetail(address.getDetail())
                        .zipcode(address.getZipcode())
                        .orderNumber(orderNum)
                        .totalPrice(totalPrice)
                        .paymentMethod("POINT")
                        .status("PAID")
                        .build()
        );


        // -------------------------------
        // 6) 주문 상세 저장 + 재고 차감
        // -------------------------------
        Set<Long> updatedProductIds = new HashSet<>();

        for (Cart c : carts) {

            Product p = c.getProduct();
            ProductOption opt = resolveCartOption(c);
            int qty = c.getQuantity();

            BigDecimal unitPrice = (opt != null) ? opt.getSellPrice() : p.getSellPrice();

            // 주문 아이템 생성
            OrderItem orderItem = orderItemRepository.save(
                    OrderItem.builder()
                            .order(order)
                            .product(p)
                            .option(opt)
                            .quantity(qty)
                            .price(unitPrice)
                            .mainImg(p.getMainImg())
                            .productName(p.getProductName())
                            .optionValue(opt != null ? opt.getOptionValue() : null)
                            .build()
            );

            // 프론트 반환 DTO
            itemDtos.add(
                    OrderItemDto.builder()
                            .orderItemId(orderItem.getOrderItemId())
                            .orderId(order.getOrderId())
                            .productId(p.getProductId())
                            .productName(orderItem.getProductName())
                            .mainImg(baseUrl + orderItem.getMainImg())
                            .optionId(opt != null ? opt.getOptionId() : null)
                            .optionValue(orderItem.getOptionValue())
                            .quantity(orderItem.getQuantity())
                            .price(orderItem.getPrice())
                            .subtotal(orderItem.getSubtotal())
                            .build()
            );

            // 재고 차감
            if (opt != null) {
                opt.setStock(opt.getStock() - qty);
                productOptionRepository.save(opt);

                updatedProductIds.add(p.getProductId()); // 옵션 상품만 대상
            } else {
                // 단일 상품은 product.stock 직접 감소
                p.setStock(p.getStock() - qty);
                productRepository.save(p);
            }
        }

        // -------------------------------
        // 7) 옵션상품 재고 합산 → Product.stock 업데이트
        // -------------------------------
        for (Long productId : updatedProductIds) {

            List<ProductOption> optionList =
                    productOptionRepository.findByProduct_ProductId(productId);

            int totalStock = optionList.stream()
                    .mapToInt(ProductOption::getStock)
                    .sum();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("상품 없음"));

            product.setStock(totalStock);
            productRepository.save(product);
        }

        // -------------------------------
        // 8) 장바구니 비우기
        // -------------------------------
        cartRepository.deleteAll(carts);


        // -------------------------------
        // 9) 최종 DTO 반환
        // -------------------------------
        return OrderDto.builder()
                .orderNumber(order.getOrderNumber())
                .totalPrice(order.getTotalPrice())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .address(MemberAddressDto.fromEntity(address))
                .items(itemDtos)
                .build();
    }

    // ================================
    // 주문 내역 조회
    // ================================
    public List<OrderDto> getOrderHistory(Long memberId) {

        String baseUrl = musinsaConfig.getImageBaseUrl();

        List<Order> orders = orderRepository.findByMember_IdOrderByCreatedAtDesc(memberId);

        List<OrderDto> dtos = new ArrayList<>();

        for (Order order : orders) {

            List<OrderItemDto> itemDtos = new ArrayList<>();

            for (OrderItem item : order.getOrderItems()) {

                String fullImg = (item.getMainImg() != null)
                        ? baseUrl + item.getMainImg()
                        : null;

                itemDtos.add(
                        OrderItemDto.builder()
                                .orderItemId(item.getOrderItemId())
                                .orderId(order.getOrderId())
                                .productId(item.getProduct().getProductId())
                                .productName(item.getProductName())
                                .mainImg(fullImg)
                                .optionValue(item.getOptionValue())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .subtotal(item.getSubtotal())
                                .build()
                );
            }

            MemberAddressDto addressDto = MemberAddressDto.builder()
                    .name(order.getReceiverName())
                    .phone(order.getReceiverPhone())
                    .address(order.getAddress())
                    .detail(order.getAddressDetail())
                    .zipcode(order.getZipcode())
                    .build();

            dtos.add(
                    OrderDto.builder()
                            .orderNumber(order.getOrderNumber())
                            .totalPrice(order.getTotalPrice())
                            .paymentMethod(order.getPaymentMethod())
                            .status(order.getStatus())
                            .address(addressDto)
                            .items(itemDtos)
                            .createdAt(order.getCreatedAt())
                            .build()
            );
        }

        return dtos;
    }

    /** Cart에 담긴 optionValue로 실제 ProductOption 찾기 */
    private ProductOption resolveCartOption(Cart cart) {

        Product product = cart.getProduct();

        // 단품 상품이면 옵션 없음
        if (!product.getIsOption()) {
            return null;
        }

        String optionValue = cart.getOptionValue();

        if (optionValue == null || optionValue.isBlank()) {
            throw new RuntimeException("옵션 상품인데 옵션 값이 비어 있습니다. productId="
                    + product.getProductId());
        }

        return product.getProductOptions().stream()
                .filter(o -> optionValue.equals(o.getOptionValue()))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("상품에 존재하지 않는 옵션 값입니다. productId="
                                + product.getProductId() + ", optionValue=" + optionValue));
    }


    /** 카드/카카오페이 결제 */
    @Transactional
    public PaymentOrderDto checkoutForCard(Long memberId, Long addressId) {

        String baseUrl = musinsaConfig.getImageBaseUrl();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        MemberAddress address = memberAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("배송지 없음"));

        List<Cart> carts = cartRepository.findByMember_Id(memberId);
        if (carts.isEmpty()) throw new RuntimeException("장바구니가 비어 있습니다.");

        BigDecimal totalPrice = BigDecimal.ZERO;

        // 재고 체크만 수행 (차감 X)
        for (Cart c : carts) {
            Product p = c.getProduct();
            ProductOption opt = resolveCartOption(c);
            int qty = c.getQuantity();

            BigDecimal unitPrice = (opt != null) ? opt.getSellPrice() : p.getSellPrice();

            if (opt != null && opt.getStock() < qty)
                throw new RuntimeException("옵션 재고 부족: " + opt.getOptionValue());

            if (opt == null && p.getStock() < qty)
                throw new RuntimeException("상품 재고 부족: " + p.getProductName());

            totalPrice = totalPrice.add(unitPrice.multiply(BigDecimal.valueOf(qty)));
        }

        // READY 상태의 주문 생성 (결제 전)
        Order order = orderRepository.save(
                Order.builder()
                        .member(member)
                        .receiverName(address.getName())
                        .receiverPhone(address.getPhone())
                        .address(address.getAddress())
                        .addressDetail(address.getDetail())
                        .zipcode(address.getZipcode())
                        .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8))
                        .totalPrice(totalPrice)
                        .paymentMethod("CARD")
                        .status("READY")
                        .build()
        );

        return PaymentOrderDto.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .totalPrice(order.getTotalPrice())
                .paymentMethod(order.getPaymentMethod())
                .build();
    }


    /** 결제 성공 후 최종 확정 처리 */
    @Transactional
    public void completeCardPayment(Long orderId) {

        String baseUrl = musinsaConfig.getImageBaseUrl();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문 없음"));

        if (!order.getStatus().equals("READY"))
            throw new RuntimeException("이미 결제 처리된 주문입니다.");

        List<Cart> carts = cartRepository.findByMember_Id(order.getMember().getId());
        if (carts.isEmpty()) throw new RuntimeException("장바구니 없음");

        Set<Long> updatedProductIds = new HashSet<>();

        // OrderItem 생성 + 재고 차감
        for (Cart c : carts) {

            Product p = c.getProduct();
            ProductOption opt = resolveCartOption(c);
            int qty = c.getQuantity();

            BigDecimal unitPrice = (opt != null) ? opt.getSellPrice() : p.getSellPrice();

            orderItemRepository.save(
                    OrderItem.builder()
                            .order(order)
                            .product(p)
                            .option(opt)
                            .quantity(qty)
                            .price(unitPrice)
                            .productName(p.getProductName())
                            .mainImg(p.getMainImg())
                            .optionValue(opt != null ? opt.getOptionValue() : null)
                            .build()
            );

            // 재고 차감
            if (opt != null) {
                opt.setStock(opt.getStock() - qty);
                productOptionRepository.save(opt);
                updatedProductIds.add(p.getProductId());
            } else {
                p.setStock(p.getStock() - qty);
                productRepository.save(p);
            }
        }

        // 옵션상품 → Product.stock 재계산
        for (Long productId : updatedProductIds) {

            List<ProductOption> optionList =
                    productOptionRepository.findByProduct_ProductId(productId);

            int totalStock = optionList.stream()
                    .mapToInt(ProductOption::getStock)
                    .sum();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("상품 없음"));

            product.setStock(totalStock);
            productRepository.save(product);
        }

        // 장바구니 비우기
        cartRepository.deleteAll(carts);

        // 주문 상태 변경 → 결제 완료
        order.setStatus("PAID");
        orderRepository.save(order);
    }
}
