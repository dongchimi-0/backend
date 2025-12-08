package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.config.MusinsaConfig;
import com.ecommerce.project.backend.domain.*;
import com.ecommerce.project.backend.dto.*;
import com.ecommerce.project.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final MusinsaConfig musinsaConfig;

    /** -------------------------
     * 장바구니 담기
     * ------------------------- */
    @Transactional
    public CartAddResponseDto addToCart(Long memberId, CartAddRequestDto req) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("상품 없음"));

        boolean isOptionProduct = product.getIsOption(); // tinyint(1) → boolean 매핑된 걸로 가정
        List<CartItemDto> cartItemDtos = new ArrayList<>();

        if (isOptionProduct) {
            // ---------------- 옵션 상품 ----------------
            String optionValue = req.getOptionValue();

            if (optionValue == null || optionValue.isBlank()) {
                throw new IllegalArgumentException("옵션 값이 필요합니다.");
            }

            // 옵션 유효성 / 타이틀 조회
            ProductOption productOption = product.getProductOptions().stream()
                    .filter(option -> optionValue.equals(option.getOptionValue()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 옵션입니다."));

            String optionTitle = productOption.getOptionTitle(); // 예: "색상"

            // 같은 상품 + 같은 옵션이면 수량만 증가
            Optional<Cart> existing = cartRepository
                    .findByMember_IdAndProduct_ProductIdAndOptionValue(memberId, req.getProductId(), optionValue);

            Cart cart;
            if (existing.isPresent()) {
                cart = existing.get();
                int newQty = cart.getQuantity() + req.getQuantity();
                if (newQty > product.getStock()) throw new IllegalArgumentException("재고 부족");
                cart.setQuantity(newQty);
            } else {
                cart = Cart.builder()
                        .member(member)
                        .product(product)
                        .optionValue(optionValue)   // 옵션상품은 실제 값 ("블랙" 등)
                        .quantity(req.getQuantity())
                        .build();
                cartRepository.save(cart);
            }

            cartItemDtos.add(buildCartItemDto(cart, optionTitle));

        } else {
            // ---------------- 단품 상품 ----------------
            if (product.getStock() < req.getQuantity()) {
                throw new IllegalArgumentException("재고 부족");
            }

            // 단품은 옵션이 없으므로, optionValue = '' 을 기준으로 찾는다
            Optional<Cart> existing = cartRepository
                    .findByMember_IdAndProduct_ProductIdAndOptionValue(memberId, req.getProductId(), "");

            Cart cart;
            if (existing.isPresent()) {
                cart = existing.get();
                int newQty = cart.getQuantity() + req.getQuantity();
                if (newQty > product.getStock()) throw new IllegalArgumentException("재고 부족");
                cart.setQuantity(newQty);
            } else {
                cart = Cart.builder()
                        .member(member)
                        .product(product)
                        .optionValue("")            // ★ 단품 규칙: 항상 빈 문자열
                        .quantity(req.getQuantity())
                        .build();
                cartRepository.save(cart);
            }

            cartItemDtos.add(buildCartItemDto(cart, "")); // 단품은 optionTitle 도 빈 문자열
        }

        int totalPrice = cartItemDtos.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();

        int totalQuantity = cartItemDtos.stream()
                .mapToInt(CartItemDto::getQuantity)
                .sum();

        return CartAddResponseDto.builder()
                .items(cartItemDtos)
                .totalPrice(totalPrice)
                .totalQuantity(totalQuantity)
                .build();
    }


    /** -------------------------
     * 장바구니 조회
     * ------------------------- */
    @Transactional(readOnly = true)
    public CartResponseDto getCart(Long memberId) {

        List<Cart> carts = cartRepository.findByMember_Id(memberId);
        String baseUrl = musinsaConfig.getImageBaseUrl();

        List<CartItemDto> items = carts.stream().map(cart -> {

            Product p = cart.getProduct();
            boolean isOptionProduct = p.getIsOption();   // tinyint(1) → boolean 가정

            String optionValue;
            String optionTitle;

            if (!isOptionProduct) {
                // -------- 단품 규칙 --------
                optionValue = "";
                optionTitle = "";
            } else {
                // -------- 옵션 상품 규칙 --------
                optionValue = cart.getOptionValue();

                if (optionValue == null || optionValue.isBlank()) {
                    // 데이터가 잘못된 상태 → 일단 프론트 안 깨지게만
                    optionValue = "";
                    optionTitle = "";
                } else {
                    // optionValue 에 맞는 optionTitle 찾기 (for-loop 사용)
                    ProductOption matched = null;
                    for (ProductOption o : p.getProductOptions()) {
                        if (optionValue.equals(o.getOptionValue())) {
                            matched = o;
                            break;
                        }
                    }

                    optionTitle = (matched != null && matched.getOptionTitle() != null)
                            ? matched.getOptionTitle()
                            : "";
                }
            }

            String fullImg = null;
            if (p.getMainImg() != null) {
                if (p.getMainImg().startsWith("/")) {
                    fullImg = baseUrl + p.getMainImg();
                } else {
                    fullImg = baseUrl + "/" + p.getMainImg();
                }
            }

            int price = p.getSellPrice().intValue();
            boolean soldOut = p.getStock() <= 0;

            return CartItemDto.builder()
                    .cartId(cart.getCartId())
                    .productId(p.getProductId())
                    .productName(p.getProductName())
                    .thumbnail(fullImg)
                    .quantity(cart.getQuantity())
                    .price(price)
                    .stock(p.getStock())
                    .soldOut(soldOut)
                    .optionValue(optionValue)   // 단품: "", 옵션: "블랙"
                    .optionTitle(optionTitle)   // 단품: "", 옵션: "색상"
                    .build();
        }).toList();

        int totalPrice = items.stream()
                .mapToInt(i -> i.getPrice() * i.getQuantity())
                .sum();
        int totalQty = items.stream()
                .mapToInt(CartItemDto::getQuantity)
                .sum();

        return CartResponseDto.builder()
                .items(items)
                .totalPrice(totalPrice)
                .totalQuantity(totalQty)
                .build();
    }


    @Transactional
    public void changeOption(Long memberId, Long cartId, String newOptionValue) {

        // 1. 장바구니 항목 조회
        Cart cart = cartRepository.findByCartIdAndMember_Id(cartId, memberId)
                .orElseThrow(() -> new RuntimeException("장바구니 항목을 찾을 수 없습니다."));

        Product product = cart.getProduct();

        // 2. 단품이면 옵션 변경 불가
        if (!product.getIsOption()) {
            throw new IllegalArgumentException("단품 상품은 옵션을 변경할 수 없습니다.");
        }

        // 3. 옵션 값 필수
        if (newOptionValue == null || newOptionValue.isBlank()) {
            throw new IllegalArgumentException("옵션 값이 필요합니다.");
        }

        // 4. 같은 값이면 변경 불필요
        if (newOptionValue.equals(cart.getOptionValue())) {
            return;
        }

        // 5. 실제 존재하는 옵션인지 검증
        boolean exists = product.getProductOptions().stream()
                .anyMatch(opt -> newOptionValue.equals(opt.getOptionValue()));

        if (!exists) {
            throw new IllegalArgumentException("유효하지 않은 옵션입니다.");
        }

        // 6. 품목 재고 체크(옵션별 재고가 있다면 여기 추가)
        // TODO: 옵션별 재고 구조 사용 시 확장
        if (cart.getQuantity() > product.getStock()) {
            throw new IllegalArgumentException("재고 부족");
        }

        // 7. 옵션 변경
        cart.setOptionValue(newOptionValue);

        // @Transactional에 의해 flush 자동 반영
    }

    /** -------------------------
     * 수량 변경 (동시성 보호)
     * ------------------------- */
    @Transactional
    public void updateQuantity(Long memberId, Long cartId, int quantity) {

        if (quantity <= 1)
            throw new IllegalArgumentException("수량은 1 이상");

        // row-level lock
        Cart cart = cartRepository.findForUpdate(cartId, memberId)
                .orElseThrow(() -> new RuntimeException("장바구니 없음"));

        Product product = cart.getProduct();

        // 핵심 포인트: 옵션 상품인지 여부 체크
        if (product.getIsOption()) {

            // 장바구니에 저장된 옵션값 기준으로 ProductOption 찾아야 함
            ProductOption option = product.getProductOptions().stream()
                    .filter(o -> o.getOptionValue().equals(cart.getOptionValue()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("옵션 없음"));

            // 옵션 재고 기준 체크
            if (quantity > option.getStock()) {
                throw new IllegalArgumentException("재고 부족");
            }

        } else {
            // 단일 상품
            if (quantity > product.getStock()) {
                throw new IllegalArgumentException("재고 부족");
            }
        }

        // 검증 통과 → 수량 업데이트
        cart.setQuantity(quantity);
    }


    /** -------------------------
     * 삭제
     * ------------------------- */
    @Transactional
    public void delete(Long cartId, Long memberId) {

        Cart cart = cartRepository.findByCartIdAndMember_Id(cartId, memberId)
                .orElseThrow(() -> new RuntimeException("장바구니 없음"));

        cartRepository.delete(cart);  // 장바구니 항목 삭제
    }

    @Transactional
    public void clearCartByMemberId(Long memberId) {
        cartRepository.deleteByMemberId(memberId);
    }

    @Transactional
    public void clearCartBySessionId(String sessionId) {
        cartRepository.deleteBySessionId(sessionId);
    }

    /** -------------------------
     * 장바구니 엔티티(Cart) 하나를 화면으로 내려주는 DTO(CartItemDto) 로 변환
     * ------------------------- */
    private CartItemDto buildCartItemDto(Cart cart, String optionTitle) {
        Product product = cart.getProduct();

        String finalOptionTitle = (optionTitle == null) ? "" : optionTitle;
        String optionValue = (cart.getOptionValue() == null) ? "" : cart.getOptionValue();

        return CartItemDto.builder()
                .cartId(cart.getCartId())
                .productId(product.getProductId())
                .productName(product.getProductName())
                .thumbnail(product.getMainImg())
                .quantity(cart.getQuantity())
                .price(product.getSellPrice().intValue())
                .stock(product.getStock())
                .soldOut(product.getStock() <= 0)
                .optionValue(optionValue)       // 단품이면 "", 옵션상품이면 "블랙" 같은 값
                .optionTitle(finalOptionTitle)  // 단품이면 "", 옵션상품이면 "색상"
                .build();
    }

}
