package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.config.MusinsaConfig;
import com.ecommerce.project.backend.domain.*;
import com.ecommerce.project.backend.dto.ProductDto;
import com.ecommerce.project.backend.dto.ProductImageDto;
import com.ecommerce.project.backend.dto.ProductOptionDto;
import com.ecommerce.project.backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryLinkRepository categoryLinkRepository;
    private final CartRepository cartRepository;
    private Product product;

    private final MusinsaConfig musinsaConfig;

    /**
     * BASE_URL을 제거하는 메서드
     *
     * @param imageUrl: 전체 이미지 URL (예: https://image.msscdn.net/thumbnails/images/goods_img/...)
     * @return BASE_URL이 제거된 이미지 경로 (예: /thumbnails/images/goods_img/...)
     */
    private String removeBaseUrl(String imageUrl) {

        if (imageUrl == null) return null;

        // 모든 공백 제거
        imageUrl = imageUrl.trim();

        // BASE_URL 후보들 전부 제거
        String[] baseUrls = {
                "https://image.msscdn.net",
                "http://image.msscdn.net",
                "https://image.msscdn.net/",
                "http://image.msscdn.net/"
        };

        for (String base : baseUrls) {
            if (imageUrl.startsWith(base)) {
                return imageUrl.substring(base.length());
            }
        }

        // BASE_URL 내부에 포함된 경우
        for (String base : baseUrls) {
            int idx = imageUrl.indexOf(base);
            if (idx >= 0) {
                return imageUrl.substring(idx + base.length());
            }
        }

        // BASE_URL이 없으면 원본 유지
        return imageUrl;
    }

    /**
     * 상품 등록
     */
    public Product createProduct(ProductDto productDto) {

        // 1. 상품 유효성 검사
        if (productDto.getStock() < 0) {
            throw new IllegalArgumentException("재고는 음수가 될 수 없습니다.");
        }
        if (productDto.getSellPrice().compareTo(productDto.getConsumerPrice()) > 0) {
            throw new IllegalArgumentException("판매가는 소비자가보다 높을 수 없습니다.");
        }

        // 2. ProductDto를 사용하여 Product 객체 생성
        Product product = new Product(productDto);

        // 3. categoryCode를 받아서 Category 찾기
        String categoryCode = productDto.getCategoryCode();
        Category category = categoryRepository.findByCategoryCode(categoryCode)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 카테고리입니다."));

        Product savedProduct = productRepository.save(product);

        // 4. 상품 옵션 처리
        List<ProductOption> options;
        AtomicInteger totalStock = new AtomicInteger(0);

        if (productDto.getIsOption() != null && productDto.getIsOption()) {
            options = productDto.getOptions().stream()
                    .map(optionDto -> {
                        totalStock.addAndGet(optionDto.getStock());  // AtomicInteger로 값을 더함
                        return new ProductOption(optionDto, savedProduct);// DTO -> Entity 변환
                    })
                    .collect(Collectors.toList());
            productOptionRepository.saveAll(options);  // 옵션 일괄 저장
        }

        // 상품의 총 재고 갱신
        if (productDto.getIsOption() != null && productDto.getIsOption()) {
            // 옵션 상품: 옵션들의 재고 합산
            savedProduct.setStock(totalStock.get());
        } else {
            // 단일 상품: DTO에서 입력한 재고값 사용
            savedProduct.setStock(productDto.getStock());
        }

        // 5. 대표 이미지 저장 (mainImg 처리)
        if (productDto.getMainImg() != null && !productDto.getMainImg().isEmpty()) {
            String imageUrl = removeBaseUrl(productDto.getMainImg());  // BASE_URL 제거

            savedProduct.setMainImg(imageUrl);

            ProductImage mainImage = ProductImage.builder()
                    .imageUrl(imageUrl)  // BASE_URL을 제거한 경로 저장
                    .sortOrder(1)  // 대표 이미지는 정렬순서 1로 설정
                    .product(savedProduct)  // 상품과 연결
                    .build();
            productImageRepository.save(mainImage);  // 대표 이미지 저장
        }

        // 6. 상세 이미지 처리 (subImages 처리)
        if (productDto.getSubImages() != null && !productDto.getSubImages().isEmpty()) {
            int sortOrder = 2;  // 상세 이미지의 시작 sortOrder 값 (1은 대표 이미지 사용)

            // 각 imageUrl을 ProductImage 객체로 변환하여 저장
            for (ProductImageDto productImageDto : productDto.getSubImages()) {
                String imageUrl = removeBaseUrl(productImageDto.getImageUrl());  // BASE_URL 제거

                ProductImage productImage = ProductImage.builder()
                        .imageUrl(imageUrl)  // BASE_URL을 제거한 경로 저장
                        .sortOrder(sortOrder++)  // 순서대로 증가
                        .product(savedProduct)  // 상품과 연결
                        .build();
                productImageRepository.save(productImage);  // 이미지 저장
            }
        }

        // 8. 카테고리와 상품 연결 (CategoryLink 저장)
        CategoryLink categoryLink = new CategoryLink(savedProduct, category.getCategoryCode());
        categoryLinkRepository.save(categoryLink);  // 카테고리 링크 저장

        return savedProduct;  // 저장된 Product 반환
    }

    /**
     * 상품 수정
     */
    @Transactional(rollbackOn = Exception.class)
    public Product updateProduct(Long productId, ProductDto productDto) {

        // 1. 기존 상품 조회
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        // 2. 재고/가격 검증
        if (productDto.getSellPrice() != null
                && productDto.getConsumerPrice() != null
                && productDto.getSellPrice().compareTo(productDto.getConsumerPrice()) > 0) {
            throw new IllegalArgumentException("판매가는 소비자가보다 높을 수 없습니다.");
        }

        Integer reqStock = (productDto.getStock() == null || productDto.getStock() < 0)
                ? 0
                : productDto.getStock();

        // 3. 메인 이미지 BASE_URL 제거
        productDto.setMainImg(removeBaseUrl(productDto.getMainImg()));

        // 4. 삭제 요청된 상세 이미지 제거
        if (productDto.getSubImagesToDelete() != null && !productDto.getSubImagesToDelete().isEmpty()) {
            productImageRepository.deleteAllByImageIdIn(productDto.getSubImagesToDelete());
        }

        // 5. 상세 이미지 전체 삭제 후 재삽입
        if (productDto.getSubImages() != null) {

            // 기존 상세 이미지 전부 삭제
            productImageRepository.deleteAllByProduct_ProductId(productId);

            int sortOrder = 1;
            for (ProductImageDto dto : productDto.getSubImages()) {
                ProductImage img = dto.toEntity(existingProduct);
                img.setImageUrl(removeBaseUrl(dto.getImageUrl()));
                img.setSortOrder(sortOrder++);
                productImageRepository.save(img);
            }
        }

    /* ============================
       6. 옵션 처리
       ============================ */

        List<Long> deleteIds = productDto.getDeleteOptionIds() == null
                ? List.of()
                : productDto.getDeleteOptionIds();

        AtomicInteger totalStock = new AtomicInteger(0);

        boolean isOptionMode = Boolean.TRUE.equals(productDto.getIsOption());
        boolean hasOptions = productDto.getOptions() != null && !productDto.getOptions().isEmpty();

        // ① 옵션상품인 경우 + 옵션 리스트가 비어있지 않은 경우만 처리
        if (isOptionMode && hasOptions) {

            // 삭제 대상 옵션 제거
            if (!deleteIds.isEmpty()) {
                productOptionRepository.deleteAllById(deleteIds);
            }

            // 옵션 업데이트 / 추가
            for (ProductOptionDto optDto : productDto.getOptions()) {

                if (optDto.getOptionId() != null && deleteIds.contains(optDto.getOptionId())) {
                    continue;
                }

                if (optDto.getOptionId() != null) {
                    ProductOption existing = productOptionRepository.findById(optDto.getOptionId()).orElse(null);
                    if (existing != null) {
                        existing.updateFromDto(optDto);
                        totalStock.addAndGet(existing.getStock());
                    }
                } else {
                    ProductOption newOpt = new ProductOption(optDto, existingProduct);
                    productOptionRepository.save(newOpt);
                    totalStock.addAndGet(newOpt.getStock());
                }
            }

            existingProduct.setIsOption(true);
            existingProduct.setStock(totalStock.get());
        }

        // ② 옵션이 아예 없는 경우 → deleteOptionIds 무시하고 전체 옵션 삭제
        else {

            productOptionRepository.deleteAllByProduct_ProductId(productId);

            existingProduct.setIsOption(false);
            existingProduct.setStock(reqStock);
        }

    /* ============================
       7. 카테고리 재맵핑
       ============================ */
        categoryLinkRepository.deleteAllByProduct_ProductId(productId);

        Category newCategory = categoryRepository.findByCategoryCode(productDto.getCategoryCode())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 카테고리입니다."));

        categoryLinkRepository.save(new CategoryLink(existingProduct, newCategory.getCategoryCode()));

    /* ============================
       8. 나머지 기본 정보 업데이트
       ============================ */
        existingProduct.updateProductInfo(productDto);

        return productRepository.save(existingProduct);
    }

    @Transactional
    public void deleteProduct(Long productId) {

        // 1) 존재 여부 확인
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 상품이 존재하지 않습니다. ID: " + productId));

        // 2) 장바구니 참조 여부 확인
        if (cartRepository.existsByProduct_ProductId(productId)) {
            throw new IllegalStateException("해당 상품이 사용자의 장바구니에 있어 삭제할 수 없습니다.");
        }

        // 3) 옵션 삭제
        productOptionRepository.deleteAllByProduct_ProductId(productId);

        // 4) 이미지 삭제
        productImageRepository.deleteAllByProduct_ProductId(productId);

        // 5) 카테고리 링크 삭제
        categoryLinkRepository.deleteAllByProduct_ProductId(productId);

        // 6) 상품 삭제
        productRepository.delete(product);
    }

    public List<ProductDto> getAdminProductList() {
        String baseUrl = musinsaConfig.getImageBaseUrl();

        return productRepository.findAll()  // 모든 상품 조회
                .stream()
                .map(p -> {
                    // ProductDto로 변환하면서, 이미지 URL에서 BASE_URL 제거
                    ProductDto productDto = ProductDto.fromEntity(p, baseUrl);

                    // 대표 이미지 URL에서 BASE_URL 제거
                    if (productDto.getMainImg() != null) {
                        productDto.setMainImg(removeBaseUrl(productDto.getMainImg()));  // BASE_URL 제거
                    }

                    // 서브 이미지 URL에서 BASE_URL 제거
                    if (productDto.getSubImages() != null) {
                        productDto.setSubImages(
                                productDto.getSubImages().stream()
                                        .map(subImage -> {
                                            // 각 서브 이미지에서 BASE_URL 제거
                                            subImage.setImageUrl(removeBaseUrl(subImage.getImageUrl()));
                                            return subImage;
                                        })
                                        .collect(Collectors.toList())
                        );
                    }

                    return productDto;
                })
                .collect(Collectors.toList());
    }


}