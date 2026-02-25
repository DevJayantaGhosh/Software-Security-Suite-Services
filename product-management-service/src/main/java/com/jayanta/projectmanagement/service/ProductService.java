package com.jayanta.projectmanagement.service;

import com.jayanta.projectmanagement.dto.*;
import com.jayanta.projectmanagement.exception.ProductNotFoundException;
import com.jayanta.projectmanagement.model.Product;
import com.jayanta.projectmanagement.model.ProductStatus;
import com.jayanta.projectmanagement.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    // 1. CREATE - JWT authenticated user only
    public Product createProduct(CreateProductDto dto, String createdBy) {
        Product product = new Product();
        product.setId(UUID.randomUUID().toString());
        product.setName(dto.getName());
        product.setVersion(dto.getVersion());
        product.setIsOpenSource(dto.isOpenSource());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getProductDirector() != null) product.setProductDirector(dto.getProductDirector());
        if (dto.getSecurityHead() != null) product.setSecurityHead(dto.getSecurityHead());
        product.setReleaseEngineers(dto.getReleaseEngineers() != null ? dto.getReleaseEngineers() : List.of());
        product.setRepos(dto.getRepos() != null ? dto.getRepos() : List.of());
        product.setDependencies(dto.getDependencies() != null ? dto.getDependencies() : List.of());
        product.setCreatedBy(createdBy);
        product.setStatus(ProductStatus.Pending);
        return productRepository.save(product);
    }

    // 2. UPDATE - SAFE PARTIAL (nulls preserved)
    public Product updateProduct(String id, Product updateData, String updatedBy) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));

        //  SAFE PARTIAL UPDATE - Only non-null fields
        if (updateData.getName() != null) existing.setName(updateData.getName());
        if (updateData.getVersion() != null) existing.setVersion(updateData.getVersion());
        if (updateData.getDescription() != null) existing.setDescription(updateData.getDescription());
        if (updateData.getIsOpenSource() != null) existing.setIsOpenSource(updateData.getIsOpenSource());
        if (updateData.getProductDirector() != null) existing.setProductDirector(updateData.getProductDirector());
        if (updateData.getSecurityHead() != null) existing.setSecurityHead(updateData.getSecurityHead());
        if (updateData.getReleaseEngineers() != null) existing.setReleaseEngineers(updateData.getReleaseEngineers());
        if (updateData.getRepos() != null) existing.setRepos(updateData.getRepos());
        if (updateData.getDependencies() != null) existing.setDependencies(updateData.getDependencies());
        if (updateData.getStatus() != null) existing.setStatus(updateData.getStatus());
        if (updateData.getRemark() != null) existing.setRemark(updateData.getRemark());
        if (updateData.getSecurityScanReportPath() != null) existing.setSecurityScanReportPath(updateData.getSecurityScanReportPath());
        if (updateData.getSignatureFilePath() != null) existing.setSignatureFilePath(updateData.getSignatureFilePath());
        if (updateData.getPublicKeyFilePath() != null) existing.setPublicKeyFilePath(updateData.getPublicKeyFilePath());
        existing.setUpdatedBy(updatedBy);

        return productRepository.save(existing);
    }

    // 3. GET ONE
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
    }

    // 4. GET ALL (Lightweight)
    public PaginationResponse<ProductListDto> getAllProductsPaginated(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var pageResult = productRepository.findAllProjection(pageable);

        return PaginationResponse.<ProductListDto>builder()
                .currentPage(pageResult.getNumber())
                .totalPages(pageResult.getTotalPages())
                .totalItems(pageResult.getTotalElements())
                .pageSize(pageResult.getSize())
                .items(pageResult.getContent())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
    }

    // 5. GET OPEN SOURCE (Lightweight)
    public List<ProductListDto> getOpenSourceProducts() {
        return productRepository.findOpenSourceProjection();
    }

    // 6. DELETE - INLINE validation
    public void deleteProduct(String id, String deletedBy) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        productRepository.delete(product);
    }
}
