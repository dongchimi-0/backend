package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.config.MusinsaConfig;
import com.ecommerce.project.backend.domain.Product;
import com.ecommerce.project.backend.domain.ProductOption;
import com.ecommerce.project.backend.domain.CategoryLink;
import com.ecommerce.project.backend.dto.OptionDto;
import com.ecommerce.project.backend.dto.ProductDetailResponseDto;
import com.ecommerce.project.backend.dto.ProductDto;
import com.ecommerce.project.backend.repository.ProductImageRepository;
import com.ecommerce.project.backend.repository.ProductOptionRepository;
import com.ecommerce.project.backend.repository.ProductRepository;
import com.ecommerce.project.backend.repository.CategoryLinkRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final MusinsaConfig musinsaConfig;
    private final ProductOptionRepository optionRepository;
    private final CategoryLinkRepository categoryLinkRepository;   // ⭐ 추가됨!
    private final ProductImageRepository productImageRepository;
    @Autowired
    private final CategoryTreeService categoryTreeService;



    /** 전체 상품 (노출 중인 상품만) */
    public List<ProductDto> getAllVisibleProducts() {
        String baseUrl = musinsaConfig.getImageBaseUrl();
        List<Product> products = productRepository.findAllVisibleProducts();

        if (products == null || products.isEmpty()) {
            System.out.println("[ProductService] 노출 중인 상품이 없습니다.");
            return List.of();
        }

        System.out.println("[ProductService] 조회된 상품 수: " + products.size());

        return products.stream()
                .map(p -> ProductDto.fromEntity(p, baseUrl))
                .collect(Collectors.toList());
    }


    /** 기존 단일상품 조회 (리스트용) */
    public ProductDto getProductById(Long id) {
        String baseUrl = musinsaConfig.getImageBaseUrl();

        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + id));
        return ProductDto.fromEntity(p, baseUrl);
    }


    /** 상품명 검색 */
    public List<ProductDto> searchProductsByName(String keyword) {
        String baseUrl = musinsaConfig.getImageBaseUrl();

        return productRepository.findByProductNameContaining(keyword)
                .stream()
                .map(p -> ProductDto.fromEntity(p, baseUrl))
                .collect(Collectors.toList());
    }


    /** 카테고리 코드별 조회 */
    public List<ProductDto> getProductsByCategoryCode(String categoryCode) {
        String baseUrl = musinsaConfig.getImageBaseUrl();

        return productRepository.findByCategoryCode(categoryCode)
                .stream()
                .map(p -> ProductDto.fromEntity(p, baseUrl))
                .collect(Collectors.toList());
    }


    /** 상품 상태 업데이트 */
    public void updateProductStatus(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품 없음"));

        boolean isOption = product.getIsOption();

        // 단일상품 처리
        if (!isOption) {
            product.setProductStatus(product.getStock() <= 0 ? 20 : 10);
            return;
        }

        // 옵션상품 처리
        List<ProductOption> options = optionRepository.findByProduct_ProductId(productId);

        int totalStock = options.stream()
                .mapToInt(ProductOption::getStock)
                .sum();

        product.setStock(totalStock);

        boolean allSoldOut = options.stream()
                .allMatch(o -> o.getStock() <= 0);

        product.setProductStatus(allSoldOut ? 20 : 10);
    }


    /** ⭐ 상세 상품 조회 (카테고리 + 옵션 포함) */
    public ProductDetailResponseDto getProductDetail(Long productId) {

        String baseUrl = musinsaConfig.getImageBaseUrl();

        // 1) 상품
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + productId));

        // 2) 옵션 DTO 변환
        List<ProductOption> options = optionRepository.findByProduct_ProductId(productId);

        List<OptionDto> optionDtos = options.stream()
                .map(o -> OptionDto.builder()
                        .optionId(o.getOptionId())
                        .optionType(o.getOptionType())
                        .optionTitle(o.getOptionTitle())
                        .optionValue(o.getOptionValue())
                        .colorCode(o.getColorCode())
                        .build()
                )
                .collect(Collectors.toList());

        // 3) 카테고리 조회
        List<String> categoryCodes = categoryLinkRepository
                .findByProduct_ProductId(productId)
                .stream()
                .map(CategoryLink::getCategoryCode)
                .collect(Collectors.toList());

        String categoryPath = null;
        if (!categoryCodes.isEmpty()) {
            categoryPath = categoryTreeService.getCategoryPath(categoryCodes.get(0));
        }

        // product_image 테이블에서 서브 이미지 조회
        List<String> subImages = productImageRepository
                .findByProduct_ProductIdOrderBySortOrderAsc(productId)
                .stream()
                .map(img -> img.getImageUrl())
                .collect(Collectors.toList());


        // 4) 응답 DTO 생성
        return ProductDetailResponseDto.builder()
                .productId(p.getProductId())
                .productName(p.getProductName())
                .description(p.getDescription())
                .consumerPrice(p.getConsumerPrice())
                .sellPrice(p.getSellPrice())
                .stock(p.getStock())
                .isOption(p.getIsOption())
                .mainImg(p.getMainImg())
                .subImages(subImages)
                .productStatus(p.getProductStatus())
                .isShow(p.getIsShow())
                .categoryPath(categoryPath)
                .categories(categoryCodes)
                .options(optionDtos)
                .build();
    }

}
