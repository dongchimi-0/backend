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
        String baseUrl = musinsaConfig.getImageBaseUrl();

        if (imageUrl != null && imageUrl.startsWith(baseUrl)) {
            return imageUrl.substring(baseUrl.length());  // BASE_URL을 제거한 나머지 경로 반환
        }

        return imageUrl;  // 만약 BASE_URL이 없다면 원래 URL을 그대로 반환
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

        // 4. 상품 옵션 처리
//        List<ProductOption> options = new ArrayList<>();
        List<ProductOption> options;
        AtomicInteger totalStock = new AtomicInteger(0);

        if (productDto.getIsOption() != null && productDto.getIsOption()) {
            options = productDto.getOptions().stream()
                    .map(optionDto -> {
                        totalStock.addAndGet(optionDto.getStock());  // AtomicInteger로 값을 더함
                        return new ProductOption(optionDto, product);  // DTO -> Entity 변환
                    })
                    .collect(Collectors.toList());
            productOptionRepository.saveAll(options);  // 옵션 일괄 저장
        }

        // 상품의 총 재고 갱신
        product.setStock(totalStock.get());  // AtomicInteger의 값을 상품의 stock에 반영
        Product savedProduct = productRepository.save(product);

        // 5. 대표 이미지 저장 (mainImg 처리)
        if (productDto.getMainImg() != null && !productDto.getMainImg().isEmpty()) {
            String imageUrl = removeBaseUrl(productDto.getMainImg());  // BASE_URL 제거

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
    public Product updateProduct(Long productId, ProductDto productDto) {

        // 1. 기존 상품 조회
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        // 2. 상품 유효성 검사 (재고, 가격 등)
        if (productDto.getStock() < 0) {
            throw new IllegalArgumentException("재고는 음수가 될 수 없습니다.");
        }
        if (productDto.getSellPrice().compareTo(productDto.getConsumerPrice()) > 0) {
            throw new IllegalArgumentException("판매가는 소비자가보다 높을 수 없습니다.");
        }

        // 3. 상품의 메인 이미지 URL에서 BASE_URL 제거
        String imageUrl = productDto.getMainImg();  // 상품 DTO에서 이미지 URL 가져오기
        String dbImageUrl = removeBaseUrl(imageUrl);  // BASE_URL 제거
        productDto.setMainImg(dbImageUrl);  // 수정된 URL을 DTO에 반영

        // 4. 기존 상세 이미지 삭제 처리 (이미지 ID를 받아서 처리)
        if (productDto.getSubImagesToDelete() != null && !productDto.getSubImagesToDelete().isEmpty()) {
            productImageRepository.deleteAllByImageIdIn(productDto.getSubImagesToDelete());  // 삭제할 이미지 ID 리스트로 삭제
        }

        // 5. 상품 이미지 테이블에서 이미지 URL 처리
        List<ProductImage> productImages = productImageRepository.findByProduct_ProductIdOrderBySortOrderAsc(productId);
        productImages.forEach(productImage -> {
            String imageUrlInProductImage = productImage.getImageUrl();  // ProductImage 테이블에서 이미지 URL 가져오기
            String updatedImageUrl = removeBaseUrl(imageUrlInProductImage);  // BASE_URL 제거
            productImage.setImageUrl(updatedImageUrl);  // 수정된 URL을 저장
        });

        // 6. 상세 이미지 처리 (subImages 처리)
        if (productDto.getSubImages() != null && !productDto.getSubImages().isEmpty()) {
            int sortOrder = 2;  // 상세 이미지의 시작 sortOrder 값 (1은 대표 이미지 사용)

            // 기존의 상세 이미지들을 삭제하고 새로운 이미지들 추가
            productImageRepository.deleteAllByProduct_ProductId(productId);  // 기존 이미지 삭제

            for (ProductImageDto productImageDto : productDto.getSubImages()) {
                // subImages가 String의 리스트이므로, URL에서 BASE_URL을 제거한 후 저장
                String imageUrlInSubImage = removeBaseUrl(productImageDto.getImageUrl());  // BASE_URL 제거
                ProductImage productImage = productImageDto.toEntity(existingProduct);  // DTO -> Entity 변환
                productImage.setImageUrl(imageUrlInSubImage);  // 이미지 URL 업데이트
                productImageRepository.save(productImage);  // 이미지 저장
            }
        }

        // 7. 상품 옵션 처리
//        List<ProductOption> options = new ArrayList<>();
        List<ProductOption> options;
        AtomicInteger totalStock = new AtomicInteger(0);

        // 기존 옵션 삭제 (옵션이 있으면 삭제)
        if (productDto.getIsOption() != null && productDto.getIsOption()) {
            productOptionRepository.deleteAllByProduct_ProductId(productId);  // 기존 옵션 삭제

            // 새 옵션 추가
            options = productDto.getOptions().stream()
                    .map(optionDto -> {
                        totalStock.addAndGet(optionDto.getStock());
                        return new ProductOption(optionDto, existingProduct);  // DTO -> Entity 변환
                    })
                    .collect(Collectors.toList());

            productOptionRepository.saveAll(options);  // 새 옵션 일괄 저장
        }

        // 8. 총 재고 처리 (옵션 상품일 경우 옵션 재고 합산, 아니면 상품Dto에서 받은 재고로 업데이트)
        if (existingProduct.getIsOption()) {
            existingProduct.getOptions().stream()
                    .forEach(option -> totalStock.addAndGet(option.getStock()));  // 각 옵션의 재고를 더합니다.
        } else {
            totalStock.set(existingProduct.getStock());  // 옵션이 없으면 기본 상품의 재고를 설정합니다.
        }

        // 상품의 총 재고 갱신
        existingProduct.setStock(totalStock.get());  // AtomicInteger의 값을 상품의 stock에 반영

        // 9. 카테고리 처리 (상품과 연결된 카테고리 수정)
        List<CategoryLink> categoryLinks = categoryLinkRepository.findByProduct_ProductId(productId);
        CategoryLink categoryLink = categoryLinks.stream()
                .findFirst()  // List에서 첫 번째 항목을 가져옵니다.
                .orElseThrow(() -> new RuntimeException("CategoryLink not found for productId: " + productId));  // 값이 없을 경우 예외 처리
        CategoryLink updatedCategoryLink = new CategoryLink(categoryLink.getProduct(), productDto.getCategoryCode());
        categoryLinkRepository.save(updatedCategoryLink);  // 새로운 객체 저장

        // 10. 상품 정보 업데이트 (상품 이름, 가격 등 기본 정보 업데이트)
        existingProduct.updateProductInfo(productDto);

        // 11. 수정된 상품 저장 (모든 수정 사항을 반영하여 상품 저장)
        return productRepository.save(existingProduct);  // 수정된 상품 반환
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