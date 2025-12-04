package com.ecommerce.project.backend.dto;

import com.ecommerce.project.backend.domain.ProductOption;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class OptionDto {
    private Long optionId;
    private Long productId;
    private String optionType;
    private String optionTitle;
    private String optionValue;
    private String colorCode;
    private Boolean isShow;  // isShow 필드 정의 (Boolean 타입)

    private BigDecimal sellPrice;
    private BigDecimal consumerPrice;
    private Integer stock;

    // getIsShow() 메서드를 정의
    public Boolean getIsShow() {
        return isShow;  // isShow 필드 값을 반환
    }

    // getProductId 메서드 추가
    public Long getProductId() {
        return productId;
    }

    public static OptionDto fromEntity(ProductOption o) {
        return OptionDto.builder()
                .optionId(o.getOptionId())
                .optionType(o.getOptionType())
                .optionTitle(o.getOptionTitle())
                .optionValue(o.getOptionValue())
                .colorCode(o.getColorCode())
                .isShow(o.getIsShow())  // isShow 값 포함
                .sellPrice(o.getSellPrice())
                .consumerPrice(o.getConsumerPrice())
                .stock(o.getStock())
                .build();
    }



}

