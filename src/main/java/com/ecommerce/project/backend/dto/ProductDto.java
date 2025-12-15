package com.ecommerce.project.backend.dto;

import com.ecommerce.project.backend.domain.Product;
import com.ecommerce.project.backend.domain.ProductOption;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    private Long productId;
    private String productName;
    private String description;
    private BigDecimal consumerPrice;
    private BigDecimal sellPrice;
    private Integer stock;
    private Boolean isOption;
    private String thumbnailUrl;

    @JsonProperty("mainImg")
    private String mainImg;

    private Integer productStatus;
    private Boolean isShow;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private List<ProductImageDto> subImages;
    private List<ProductOptionDto> options;

    private String categoryCode;

    private List<Long> subImagesToDelete;

    private Long likeCount;    // 좋아요 개수
    private Boolean userLiked; // 로그인 사용자가 좋아요 눌렀는지 여부

    private List<Long> deleteOptionIds; // 삭제할 옵션

    // ProductDto 클래스에서 options 필드를 처리하는 코드
    public static ProductDto fromEntity(Product p, String baseUrl) {

        String catCode = null;
        if (p.getCategoryLinks() != null && !p.getCategoryLinks().isEmpty()) {
            catCode = p.getCategoryLinks().get(0).getCategoryCode();
        }

        // 메인 이미지 URL
        String fullUrl = null;
        if (p.getMainImg() != null) {
            if (p.getMainImg().startsWith("/")) {
                fullUrl = baseUrl + p.getMainImg();
            } else {
                fullUrl = baseUrl + "/" + p.getMainImg();
            }
        }

        // 옵션 리스트
        List<ProductOptionDto> optionList = new ArrayList<>();
        if (p.getOptions() != null) {  // getOptions() 메서드 호출
            optionList = p.getOptions().stream()
                    .map(ProductOptionDto::fromEntity)
                    .collect(Collectors.toList());
        }

        // 서브 이미지 URL
        List<ProductImageDto> subImageList = new ArrayList<>();
        if (p.getImages() != null) {
            subImageList = p.getImages().stream()
                    .map(img -> new ProductImageDto(
                            img.getImageUrl().startsWith("/") ? baseUrl + img.getImageUrl() : baseUrl + "/" + img.getImageUrl(),
                            img.getSortOrder(),
                            img.getProductId()
                    ))
                    .collect(Collectors.toList());
        }

        // ----------- DTO 변환 -----------
        ProductDto dto = ProductDto.builder()
                .productId(p.getProductId())
                .productName(p.getProductName())
                .description(p.getDescription())
                .consumerPrice(p.getConsumerPrice())
                .sellPrice(p.getSellPrice())
                .stock(p.getStock())
                .isOption(p.getIsOption())
                .mainImg(fullUrl)
                .productStatus(p.getProductStatus())
                .isShow(p.getIsShow())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .subImages(subImageList)
                .options(optionList)
                .categoryCode(catCode)
                .build();

        // ⭐ 좋아요 정보는 서비스에서 set 해줌
        return dto;
    }





}

