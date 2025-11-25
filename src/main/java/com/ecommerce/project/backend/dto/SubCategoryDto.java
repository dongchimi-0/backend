package com.ecommerce.project.backend.dto;

import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubCategoryDto {
    private String title;
    private Map<String, String> children;  // leafCode → leafName
}
