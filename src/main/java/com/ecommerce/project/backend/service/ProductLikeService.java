package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.domain.ProductLike;
import com.ecommerce.project.backend.repository.ProductLikeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductLikeService {

    private final ProductLikeRepository likeRepository;

    @Transactional
    public boolean toggleLike(Long memberId, Long productId) {

        boolean exists = likeRepository.existsByMemberIdAndProductId(memberId, productId);

        if (exists) {
            likeRepository.deleteByMemberIdAndProductId(memberId, productId);
            return false; // 좋아요 취소
        }

        ProductLike like = ProductLike.builder()
                .memberId(memberId)
                .productId(productId)
                .build();

        likeRepository.save(like);
        return true; // 좋아요 추가
    }

    public List<ProductLike> getMyLikes(Long memberId) {
        return likeRepository.findByMemberId(memberId);
    }
}
