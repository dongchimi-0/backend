package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.config.MusinsaConfig;
import com.ecommerce.project.backend.domain.*;
import com.ecommerce.project.backend.dto.*;
import com.ecommerce.project.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 주문 번호 생성
    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String random = UUID.randomUUID().toString().substring(0, 6);
    String orderNum = "YDJ-" + date + "-" + random;

    @Transactional
    public OrderDto checkout(Long memberId, OrderRequestDTO req) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        MemberAddress address = memberAddressRepository.findById(req.getAddressId())
                .orElseThrow(() -> new RuntimeException("배송지 없음"));

        List<OrderItemDto> itemDtos = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        // 먼저 totalPrice를 계산
        for (OrderRequestDTO.Item it : req.getItems()) {

            Product product = productRepository.findById(it.getProductId())
                    .orElseThrow(() -> new RuntimeException("상품 없음"));

            Integer quantity = it.getQuantity();
            ProductOption option = null;

            String displayOptionValue = "기본";

            // 옵션 문자열 매칭
            if (it.getOptionValues() != null &&
                    !it.getOptionValues().isEmpty() &&
                    !it.getOptionValues().get(0).equals("기본")) {

                String raw = it.getOptionValues().get(0);  // "색상 Ivory"
                String[] parts = raw.split(" ", 2);

                if (parts.length == 2) {
                    String title = parts[0].trim();
                    String value = parts[1].trim();

                    option = product.getProductOptions().stream()
                            .filter(o ->
                                    o.getOptionTitle().equalsIgnoreCase(title) &&
                                            o.getOptionValue().equalsIgnoreCase(value)
                            )
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("옵션 없음"));

                    displayOptionValue = option.getOptionTitle() + " " + option.getOptionValue();
                }
            }

            BigDecimal price = (option != null && option.getSellPrice() != null)
                    ? option.getSellPrice()
                    : product.getSellPrice();

            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
            totalPrice = totalPrice.add(subtotal);
        }

        // 회원 포인트가 충분한지 체크
        if (member.getPoint() < totalPrice.intValue()) {
            throw new RuntimeException("포인트가 부족합니다.");
        }

        // 포인트 차감
        member.setPoint(member.getPoint() - totalPrice.intValue());
        memberRepository.save(member);

        // totalPrice가 계산된 후 Order INSERT
        Order order = orderRepository.save(
                Order.builder()
                        .member(member)
                        .receiverName(address.getName())
                        .receiverPhone(address.getPhone())
                        .address(address.getAddress())
                        .addressDetail(address.getDetail())
                        .zipcode(address.getZipcode())
                        .orderNumber(orderNum)
                        .status("PAID")
                        .paymentMethod("POINT")
                        .totalPrice(totalPrice)
                        .build()
        );

        // OrderItem 저장 + 재고 차감
        for (OrderRequestDTO.Item it : req.getItems()) {

            Product product = productRepository.findById(it.getProductId())
                    .orElseThrow(() -> new RuntimeException("상품 없음"));

            Integer quantity = it.getQuantity();
            ProductOption option = null;
            String displayOptionValue = "기본";

            if (it.getOptionValues() != null &&
                    !it.getOptionValues().isEmpty() &&
                    !it.getOptionValues().get(0).equals("기본")) {

                String raw = it.getOptionValues().get(0);
                String[] parts = raw.split(" ", 2);

                if (parts.length == 2) {
                    String title = parts[0].trim();
                    String value = parts[1].trim();

                    option = product.getProductOptions().stream()
                            .filter(o ->
                                    o.getOptionTitle().equalsIgnoreCase(title) &&
                                            o.getOptionValue().equalsIgnoreCase(value)
                            )
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("옵션 없음"));

                    displayOptionValue = option.getOptionTitle() + " " + option.getOptionValue();
                }
            }

            BigDecimal price = (option != null && option.getSellPrice() != null)
                    ? option.getSellPrice()
                    : product.getSellPrice();

            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));

            // 재고 차감
            if (option != null) {
                if (option.getStock() < quantity) throw new RuntimeException("옵션 재고 부족");
                option.setStock(option.getStock() - quantity);
                productOptionRepository.save(option);

                // 옵션 재고 변경 후 product.stock 재계산
                product.updateTotalStockFromOptions();
                productRepository.save(product);

            } else {
                if (product.getStock() < quantity) throw new RuntimeException("상품 재고 부족");
                product.setStock(product.getStock() - quantity);
                productRepository.save(product);
            }

            OrderItem orderItem = orderItemRepository.save(
                    OrderItem.builder()
                            .order(order)
                            .product(product)
                            .quantity(quantity)
                            .price(price)
                            .mainImg(product.getMainImg())
                            .productName(product.getProductName())
                            .optionValue(displayOptionValue)
                            .build()
            );

            itemDtos.add(
                    OrderItemDto.builder()
                            .orderItemId(orderItem.getOrderItemId())
                            .orderId(order.getOrderId())
                            .productId(product.getProductId())
                            .productName(orderItem.getProductName())
                            .mainImg(orderItem.getMainImg())
                            .quantity(quantity)
                            .price(price)
                            .subtotal(subtotal)
                            .optionId(option != null ? option.getOptionId() : null)
                            .optionValue(displayOptionValue)
                            .build()
            );
        }

        return OrderDto.builder()
                .orderNumber(order.getOrderNumber())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .address(MemberAddressDto.fromEntity(address))
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
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
    public PaymentOrderDto checkoutForCard(Long memberId, OrderRequestDTO req) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        MemberAddress address = memberAddressRepository.findById(req.getAddressId())
                .orElseThrow(() -> new RuntimeException("배송지 없음"));

        BigDecimal totalPrice = BigDecimal.ZERO;

        // items 기반으로 가격 & 재고 체크
        for (OrderRequestDTO.Item it : req.getItems()) {

            Product product = productRepository.findById(it.getProductId())
                    .orElseThrow(() -> new RuntimeException("상품 없음"));

            Integer quantity = it.getQuantity();
            ProductOption option = null;

            // 옵션 매칭
            if (it.getOptionValues() != null &&
                    !it.getOptionValues().isEmpty() &&
                    !it.getOptionValues().get(0).equals("기본")) {

                String raw = it.getOptionValues().get(0);   // "색상 Ivory"
                String[] parts = raw.split(" ", 2);

                if (parts.length == 2) {
                    String title = parts[0].trim();
                    String value = parts[1].trim();

                    option = product.getProductOptions().stream()
                            .filter(o ->
                                    o.getOptionTitle().equalsIgnoreCase(title) &&
                                            o.getOptionValue().equalsIgnoreCase(value)
                            )
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("옵션 없음"));
                }
            }

            BigDecimal price = (option != null) ? option.getSellPrice() : product.getSellPrice();

            // 🚨 재고 체크만 (차감은 결제 완료 후)
            if (option != null && option.getStock() < quantity)
                throw new RuntimeException("옵션 재고 부족");

            if (option == null && product.getStock() < quantity)
                throw new RuntimeException("상품 재고 부족");

            totalPrice = totalPrice.add(price.multiply(BigDecimal.valueOf(quantity)));
        }

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

    // Order 엔티티 조회용 메서드
    public Order getOrderEntity(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문 없음"));
    }

    /** 결제 성공 후 최종 확정 처리 */
    @Transactional
    public void completeCardPayment(VerifyPaymentRequestDto req) {

        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("주문 없음"));

        if (!order.getStatus().equals("READY"))
            throw new RuntimeException("이미 결제 처리된 주문입니다.");

        Set<Long> updatedProductIds = new HashSet<>();

        for (OrderRequestDTO.Item it : req.getItems()) {

            Product product = productRepository.findById(it.getProductId())
                    .orElseThrow(() -> new RuntimeException("상품 없음"));

            Integer quantity = it.getQuantity();
            ProductOption option = null;
            String displayOptionValue = "기본";

            // 옵션 매칭
            if (it.getOptionValues() != null &&
                    !it.getOptionValues().isEmpty() &&
                    !it.getOptionValues().get(0).equals("기본")) {

                String raw = it.getOptionValues().get(0);  // "색상 Ivory"
                String[] parts = raw.split(" ", 2);

                if (parts.length == 2) {
                    String title = parts[0].trim();
                    String value = parts[1].trim();

                    option = product.getProductOptions().stream()
                            .filter(o ->
                                    o.getOptionTitle().equalsIgnoreCase(title) &&
                                            o.getOptionValue().equalsIgnoreCase(value)
                            )
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("옵션 없음"));

                    displayOptionValue = option.getOptionTitle() + " " + option.getOptionValue();
                }
            }

            BigDecimal price = (option != null) ? option.getSellPrice() : product.getSellPrice();

            // OrderItem 생성
            orderItemRepository.save(
                    OrderItem.builder()
                            .order(order)
                            .product(product)
                            .quantity(quantity)
                            .price(price)
                            .productName(product.getProductName())
                            .mainImg(product.getMainImg())
                            .optionValue(displayOptionValue)
                            .build()
            );

            // 재고 차감
            if (option != null) {
                option.setStock(option.getStock() - quantity);
                productOptionRepository.save(option);
                updatedProductIds.add(product.getProductId());
            } else {
                product.setStock(product.getStock() - quantity);
                productRepository.save(product);
            }
        }

        // 옵션상품일 경우 product.stock 재계산
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

        // 결제 완료 처리
        order.setStatus("PAID");
        orderRepository.save(order);
    }
}
