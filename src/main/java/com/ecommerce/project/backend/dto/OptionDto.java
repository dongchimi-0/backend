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
    private String optionType;
    private String optionTitle;
    private String optionValue;
    private String colorCode;
//    private Boolean isShow;

    private BigDecimal sellPrice;
//    private BigDecimal consumerPrice;
    private Integer stock;

    public static OptionDto fromEntity(ProductOption o) {
        return OptionDto.builder()
                .optionId(o.getOptionId())
                .optionType(o.getOptionType())
                .optionTitle(o.getOptionTitle())
                .optionValue(o.getOptionValue())
                .colorCode(o.getColorCode())
//                .isShow(o.getIsShow())
                .sellPrice(o.getSellPrice())
//                .consumerPrice(o.getConsumerPrice())
                .stock(o.getStock())

                .build();
    }

}

