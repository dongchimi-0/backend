package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.dto.AiProductRequestDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AiDescriptionService {

    private final WebClient aiWebClient; // WebClientConfig에서 만든 Bean 자동 주입

    public String generateDescription(AiProductRequestDto req) {

        Response res = aiWebClient.post()
                .uri("/ai/description")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Response.class)
                .block();

        return res.getDescription();
    }

    @Data
    public static class Response {
        private String description;
    }
}
