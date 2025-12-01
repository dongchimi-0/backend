package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.config.MusinsaConfig;
import com.ecommerce.project.backend.domain.ProductLike;
import com.ecommerce.project.backend.dto.ProductDto;
import com.ecommerce.project.backend.repository.ProductLikeRepository;
import com.ecommerce.project.backend.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductLikeService {

    private final ProductLikeRepository likeRepository;
    private final ProductRepository productRepository;
    private final MusinsaConfig musinsaConfig;

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

    public Long countLikes(Long productId) {
        return likeRepository.countByProductId(productId);
    }

    public List<ProductDto> getMyLikeProducts(Long memberId) {
        String baseUrl = musinsaConfig.getImageBaseUrl();

        return likeRepository.findByMemberId(memberId)
                .stream()
                .map(like -> productRepository.findById(like.getProductId()))
                .flatMap(Optional::stream)
                .map(product -> {
                    ProductDto dto = ProductDto.fromEntity(product, baseUrl);
                    dto.setLikeCount(likeRepository.countByProductId(product.getProductId()));
                    dto.setUserLiked(true);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
