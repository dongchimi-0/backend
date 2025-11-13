package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.domain.*;
import com.ecommerce.project.backend.dto.CartDto;
import com.ecommerce.project.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;

    @Transactional
    public CartDto addToCart(CartDto dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
        ProductOption option = dto.getOptionId() != null
                ? productOptionRepository.findById(dto.getOptionId()).orElse(null)
                : null;

        Cart cart = Cart.builder()
                .member(member)
                .product(product)
                .productOption(option)
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .isSelected(true)
                .build();

        Cart saved = cartRepository.save(cart);
        return CartDto.fromEntity(saved);
    }

    public List<CartDto> getCartByMember(Long memberId) {
        return cartRepository.findByMember_Id(memberId)
                .stream()
                .map(CartDto::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteCartItem(Long cartId) {
        cartRepository.deleteById(cartId);
    }
}


