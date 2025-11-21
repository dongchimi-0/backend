package com.ecommerce.project.backend.repository;

import com.ecommerce.project.backend.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // 회원 장바구니
    List<Cart> findByMember_Id(Long memberId);

    // 비회원 장바구니
    List<Cart> findBySessionId(String sessionId);

    // 옵션상품 중복 검사
    Optional<Cart> findByMember_IdAndProduct_ProductIdAndOption_OptionId(
            Long memberId, Long productId, Long optionId);

    // 단일상품 중복 검사
    Optional<Cart> findByMember_IdAndProduct_ProductIdAndOptionIsNull(
            Long memberId, Long productId);
}


