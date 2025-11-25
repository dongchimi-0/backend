package com.ecommerce.project.backend.dto;

import com.ecommerce.project.backend.domain.Product;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class ProductDetailDto {

    private Long productId;
    private String productName;
    private BigDecimal sellPrice;

    private String mainImg;
    private List<String> subImages;

    private boolean liked;   // ✔ 로그인한 사용자가 좋아요 눌렀는지
    private Long likeCount;  // ✔ 좋아요 수

    public static ProductDetailDto from(Product product, boolean liked, Long likeCount, String baseUrl) {

        return ProductDetailDto.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .sellPrice(product.getSellPrice())

                // 이미지 URL
                .mainImg(baseUrl + product.getMainImg())

                // 서브 이미지 리스트
                .subImages(
                        product.getImages() != null
                                ? product.getImages().stream()
                                .map(img -> baseUrl + img.getImageUrl())
                                .toList()
                                : List.of()
                )


                // 좋아요 정보들
                .liked(liked)
                .likeCount(likeCount)

                .build();
    }
}
