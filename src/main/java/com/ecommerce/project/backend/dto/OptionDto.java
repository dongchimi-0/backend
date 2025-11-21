package com.ecommerce.project.backend.dto;

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
}

