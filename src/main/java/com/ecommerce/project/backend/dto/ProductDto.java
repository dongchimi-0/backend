package com.ecommerce.project.backend.dto;

import com.ecommerce.project.backend.domain.Product;
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


    private List<String> subImages;


    public static ProductDto fromEntity(Product p, String baseUrl) {

        // 메인 이미지 URL
        String fullUrl = null;
        if (p.getMainImg() != null) {
            if (p.getMainImg().startsWith("/")) {
                fullUrl = baseUrl + p.getMainImg();
            } else {
                fullUrl = baseUrl + "/" + p.getMainImg();
            }
        }


        // 서브 이미지 URL
        List<String> subImageList = new ArrayList<>();
        if (p.getImages() != null) {
            subImageList = p.getImages().stream()
                    .map(img -> {
                        // 앞에 "/" 붙어 있으면 그대로
                        if (img.getImageUrl().startsWith("/")) {
                            return baseUrl + img.getImageUrl();
                        }
                        // 아니면 "/" 를 붙여준다
                        return baseUrl + "/" + img.getImageUrl();
                    })
                    .collect(Collectors.toList());
        }

        // ----------- DTO 변환 -----------
        return ProductDto.builder()
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
                .build();
    }


}
