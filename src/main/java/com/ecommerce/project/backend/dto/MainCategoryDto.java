package com.ecommerce.project.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainCategoryDto {
    private String code;
    private String title;
}
