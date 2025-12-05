package com.ecommerce.project.backend.dto;

import lombok.Data;

@Data
public class AiProductRequestDto {
    private String name;
    private Integer price;
    private String options;
    private String category_path;
    private String image_url;
}
