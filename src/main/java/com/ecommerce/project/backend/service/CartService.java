package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.config.MusinsaConfig;
import com.ecommerce.project.backend.domain.*;
import com.ecommerce.project.backend.dto.*;
import com.ecommerce.project.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository optionRepository;
    private final MusinsaConfig musinsaConfig;


    /** -------------------------
     * 장바구니 담기
     * ------------------------- */
    @Transactional
    public void addToCart(Long memberId, CartAddRequestDto req) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("상품 없음"));

        boolean isOptionProduct = product.getIsOption();

        /** 재고 체크 공통 함수 */
        int requestQty = req.getQuantity();

        if (isOptionProduct) {
            ProductOption option = optionRepository.findById(req.getOptionId())
                    .orElseThrow(() -> new RuntimeException("옵션 없음"));

            if (option.getStock() < requestQty)
                throw new IllegalArgumentException("재고 부족");

            Optional<Cart> existing = cartRepository
                    .findByMember_IdAndProduct_ProductIdAndOption_OptionId(memberId,
                            req.getProductId(), req.getOptionId());

            if (existing.isPresent()) {
                Cart cart = existing.get();
                int newQty = cart.getQuantity() + requestQty;
                if (newQty > option.getStock()) throw new IllegalArgumentException("재고 부족");

                cart.setQuantity(newQty);
                return;
            }

            cartRepository.save(Cart.builder()
                    .member(member)
                    .product(product)
                    .option(option)
                    .quantity(requestQty)
                    .build());

        } else {
            if (product.getStock() < requestQty)
                throw new IllegalArgumentException("재고 부족");

            Optional<Cart> existing = cartRepository
                    .findByMember_IdAndProduct_ProductIdAndOptionIsNull(memberId, req.getProductId());

            if (existing.isPresent()) {
                Cart cart = existing.get();
                int newQty = cart.getQuantity() + requestQty;
                if (newQty > product.getStock()) throw new IllegalArgumentException("재고 부족");
                cart.setQuantity(newQty);
                return;
            }

            cartRepository.save(Cart.builder()
                    .member(member)
                    .product(product)
                    .option(null)
                    .quantity(requestQty)
                    .build());
        }
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
            ProductOption o = cart.getOption();

            String fullImg = null;
            if (p.getMainImg() != null) {
                if (p.getMainImg().startsWith("/")) {
                    fullImg = baseUrl + p.getMainImg();
                } else {
                    fullImg = baseUrl + "/" + p.getMainImg();
                }
            }

            boolean soldOut = (o == null) ? p.getStock() <= 0 : o.getStock() <= 0;

            return CartItemDto.builder()
                    .cartId(cart.getCartId())
                    .productId(p.getProductId())
                    .productName(p.getProductName())
                    .thumbnail(fullImg)
                    .quantity(cart.getQuantity())
                    .price(p.getSellPrice().intValue())
                    .stock(o == null ? p.getStock() : o.getStock())
                    .soldOut(soldOut)
                    .option(o == null ? null :
                            OptionDto.builder()
                                    .optionId(o.getOptionId())
                                    .optionType(o.getOptionType())
                                    .optionTitle(o.getOptionTitle())
                                    .optionValue(o.getOptionValue())
                                    .colorCode(o.getColorCode())
                                    .build())
                    .build();
        }).toList();

        int totalPrice = items.stream().mapToInt(i -> i.getPrice() * i.getQuantity()).sum();
        int totalQty = items.stream().mapToInt(CartItemDto::getQuantity).sum();

        return CartResponseDto.builder()
                .items(items)
                .totalPrice(totalPrice)
                .totalQuantity(totalQty)
                .build();
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
        ProductOption option = cart.getOption();

        int stock = (option == null) ? product.getStock() : option.getStock();

        if (quantity > stock)
            throw new IllegalArgumentException("재고 부족");

        cart.setQuantity(quantity);
    }


    /** -------------------------
     * 옵션 변경 (동시성 + 중복병합)
     * ------------------------- */
    @Transactional
    public void changeOption(Long memberId, Long cartId, Long newOptionId) {

        Cart cart = cartRepository.findForUpdate(cartId, memberId)
                .orElseThrow(() -> new RuntimeException("장바구니 없음"));

        Product product = cart.getProduct();
        ProductOption newOption = optionRepository.findById(newOptionId)
                .orElseThrow(() -> new RuntimeException("옵션 없음"));

        /** 재고 체크 */
        if (newOption.getStock() < cart.getQuantity())
            throw new IllegalArgumentException("재고 부족");

        /** 이미 동일한 상품 + 옵션이 존재하면 merge */
        Optional<Cart> duplicate = cartRepository
                .findByMember_IdAndProduct_ProductIdAndOption_OptionId(
                        memberId,
                        product.getProductId(),
                        newOptionId
                );

        if (duplicate.isPresent()) {
            Cart dup = duplicate.get();
            dup.setQuantity(dup.getQuantity() + cart.getQuantity());

            cartRepository.delete(cart); // 원래 장바구니 제거
            return;
        }

        cart.setOption(newOption);
    }


    /** -------------------------
     * 삭제
     * ------------------------- */
    @Transactional
    public void delete(Long cartId, Long memberId) {

        Cart cart = cartRepository.findByCartIdAndMember_Id(cartId, memberId)
                .orElseThrow(() -> new RuntimeException("장바구니 없음"));

        cartRepository.delete(cart);
    }
}
