package com.ecommerce.project.backend.repository;

import com.ecommerce.project.backend.domain.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {

    boolean existsByMemberIdAndProductId(Long memberId, Long productId);

    void deleteByMemberIdAndProductId(Long memberId, Long productId);

    List<ProductLike> findByMemberId(Long memberId);

    Long countByProductId(Long productId);
}
