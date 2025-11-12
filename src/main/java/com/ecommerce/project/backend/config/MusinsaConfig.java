package com.ecommerce.project.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 무신사 이미지 베이스 URL을 환경 변수에서 불러오는 설정 클래스
 */
@Component
public class MusinsaConfig {

    @Value("${musinsa.image-base-url}")
    private String imageBaseUrl;

    public String getImageBaseUrl() {
        return imageBaseUrl;
    }
}
