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
            boolean isOptionProduct = p.getIsOption();

            // -------------------------------
            // 옵션 객체 생성
            // -------------------------------
            OptionDto optionDto = null;

            if (isOptionProduct) {
                String optionValue = cart.getOptionValue();

                if (optionValue != null && !optionValue.isBlank()) {

                    ProductOption matched = null;
                    for (ProductOption o : p.getProductOptions()) {
                        if (optionValue.equals(o.getOptionValue())) {
                            matched = o;
                            break;
                        }
                    }

                    if (matched != null) {
                        optionDto = OptionDto.fromEntity(matched);
                    }
                }
            }

            // -------------------------------
            // 이미지 URL 생성
            // -------------------------------
            String fullImg = null;
            if (p.getMainImg() != null) {
                if (p.getMainImg().startsWith("/")) {
                    fullImg = baseUrl + p.getMainImg();
                } else {
                    fullImg = baseUrl + "/" + p.getMainImg();
                }
            }

            // 상품 기본정보
            int price = p.getSellPrice().intValue();
            boolean soldOut = p.getStock() <= 0;

            // -------------------------------
            // CartItemDto 반환
            // -------------------------------
            return CartItemDto.builder()
                    .cartId(cart.getCartId())
                    .productId(p.getProductId())
                    .productName(p.getProductName())
                    .thumbnail(fullImg)
                    .quantity(cart.getQuantity())
                    .price(price)
                    .stock(p.getStock())
                    .soldOut(soldOut)
                    .option(optionDto)  // ★ 여기!! option 객체 추가
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

        if (quantity <= 0)
            throw new IllegalArgumentException("수량은 1 이상");

        /** row-level lock */
        Cart cart = cartRepository.findForUpdate(cartId, memberId)
                .orElseThrow(() -> new RuntimeException("장바구니 없음"));

        Product product = cart.getProduct();

        int stock = product.getStock();  // 옵션이 없으면 상품의 재고 사용

        if (quantity > stock)
            throw new IllegalArgumentException("재고 부족");

        cart.setQuantity(quantity);  // 수량 업데이트
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

        String optionValue = (cart.getOptionValue() == null) ? "" : cart.getOptionValue();
        String finalOptionTitle = (optionTitle == null) ? "" : optionTitle;

        OptionDto optionDto;

        if (!product.getIsOption()) {
            // 단품일 때 option 객체는 null이 아니라 빈 값으로 내려보냄
            optionDto = OptionDto.builder()
                    .optionId(null)
                    .optionType("")
                    .optionTitle("")
                    .optionValue("")
                    .colorCode("")
                    .sellPrice(product.getSellPrice())
                    .stock(product.getStock())
                    .build();

        } else {
            // 옵션 상품일 때만 productOption 찾기
            ProductOption matched = product.getProductOptions().stream()
                    .filter(o -> optionValue.equals(o.getOptionValue()))
                    .findFirst()
                    .orElse(null);

            optionDto = OptionDto.builder()
                    .optionId(matched != null ? matched.getOptionId() : null)
                    .optionType(matched != null ? matched.getOptionType() : "")
                    .optionTitle(finalOptionTitle)
                    .optionValue(optionValue)
                    .colorCode(matched != null ? matched.getColorCode() : "")
                    .sellPrice(matched != null ? matched.getSellPrice() : product.getSellPrice())
                    .stock(matched != null ? matched.getStock() : product.getStock())
                    .build();
        }

        return CartItemDto.builder()
                .cartId(cart.getCartId())
                .productId(product.getProductId())
                .productName(product.getProductName())
                .thumbnail(product.getMainImg())
                .quantity(cart.getQuantity())
                .price(product.getSellPrice().intValue())
                .stock(product.getStock())
                .soldOut(product.getStock() <= 0)
                .option(optionDto)
                .build();
    }



}
