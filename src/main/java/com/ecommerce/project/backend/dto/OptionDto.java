package com.ecommerce.project.backend.dto;

import com.ecommerce.project.backend.domain.ProductOption;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OptionDto {
    private Long optionId;
    private String optionType;
    private String optionTitle;
    private String optionValue;
    private String colorCode;

    public static OptionDto fromEntity(ProductOption o) {
        return OptionDto.builder()
                .optionId(o.getOptionId())
                .optionType(o.getOptionType())
                .optionTitle(o.getOptionTitle())
                .optionValue(o.getOptionValue())
                .colorCode(o.getColorCode())
                .build();
    }

}

