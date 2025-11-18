package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.domain.ProductOption;
import com.ecommerce.project.backend.dto.ProductOptionDto;
import com.ecommerce.project.backend.service.ProductOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/options")
@RequiredArgsConstructor
public class ProductOptionController {

    private final ProductOptionService productOptionService;

    /**
     * 1. 특정 상품의 옵션 목록 조회
     * GET /api/options/{productId}
     */
    @GetMapping("/{productId}")
    public ResponseEntity<List<ProductOptionDto>> getOptionsByProduct(@PathVariable Long productId) {
        List<ProductOption> options = productOptionService.getOptionsByProduct(productId);

        // Entity → DTO 변환
        List<ProductOptionDto> dtoList = options.stream()
                .map(ProductOptionDto::fromEntity)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    /**
     * 2. 옵션 단건 조회
     * GET /api/options/detail/{optionId}
     */
    @GetMapping("/detail/{optionId}")
    public ResponseEntity<ProductOption> getOptionDetail(@PathVariable Long optionId) {
        ProductOption option = productOptionService.getOption(optionId);
        return ResponseEntity.ok(option);
    }

    /**
     * 3. 옵션 등록
     * POST /api/options
     */
    @PostMapping
    public ResponseEntity<ProductOptionDto> createOption(@RequestBody ProductOptionDto dto) {
        ProductOptionDto saved = productOptionService.saveOption(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * 4. 옵션 수정
     * PUT /api/options/{optionId}
     */
    @PutMapping("/{optionId}")
    public ResponseEntity<ProductOption> updateOption(
            @PathVariable Long optionId,
            @RequestBody ProductOption newData
    ) {
        ProductOption updated = productOptionService.updateOption(optionId, newData);
        return ResponseEntity.ok(updated);
    }

    /**
     * 5. 옵션 삭제
     * DELETE /api/options/{optionId}
     */
    @DeleteMapping("/{optionId}")
    public ResponseEntity<Void> deleteOption(@PathVariable Long optionId) {
        productOptionService.deleteOption(optionId);
        return ResponseEntity.noContent().build();
    }
}
