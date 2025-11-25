package com.ecommerce.project.backend.dto;

import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryTreeResponseDto {
    private Map<String, CategoryDto> tree;
}
