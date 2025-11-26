package com.ecommerce.project.backend.repository;

import com.ecommerce.project.backend.domain.Cart;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByMember_Id(Long memberId);

    Optional<Cart> findByMember_IdAndProduct_ProductIdAndOption_OptionId(
            Long memberId, Long productId, Long optionId);

    Optional<Cart> findByMember_IdAndProduct_ProductIdAndOptionIsNull(
            Long memberId, Long productId);

    /** cartId + memberId 동시 확인 -> 보안 OK */
    Optional<Cart> findByCartIdAndMember_Id(Long cartId, Long memberId);

    /** 동시성 문제 해결: 수량/옵션 변경 시 row-level lock */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.cartId = :cartId and c.member.id = :memberId")
    Optional<Cart> findForUpdate(
            @Param("cartId") Long cartId,
            @Param("memberId") Long memberId);
}
