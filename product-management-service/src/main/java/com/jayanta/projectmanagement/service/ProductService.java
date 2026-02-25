package com.jayanta.projectmanagement.service;

import com.jayanta.projectmanagement.dto.*;
import com.jayanta.projectmanagement.exception.ProductNotFoundException;
import com.jayanta.projectmanagement.model.Product;
import com.jayanta.projectmanagement.model.ProductStatus;
import com.jayanta.projectmanagement.repository.ProductRepository;
import com.mongodb.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.PageRequest;
import static com.mongodb.client.model.Filters.eq;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;

    // 1. CREATE - JWT authenticated user only
    public Product createProduct(Product product, String createdBy) {
        product.setId(UUID.randomUUID().toString());
        product.setCreatedBy(createdBy);
        product.setUpdatedBy(createdBy);
        product.setStatus(ProductStatus.Pending);

        log.info("Creating product '{}' (v{}) by '{}'",
                product.getName(), product.getVersion(), createdBy);

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

    // 4. FULL Product pagination (NEWEST FIRST)
    public PaginationResponse<Product> getAllProductsPaginated(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var pageResult = productRepository.findAllByOrderByCreatedAtDesc(pageable);

        return PaginationResponse.<Product>builder()
                .currentPage(pageResult.getNumber())
                .totalPages(pageResult.getTotalPages())
                .totalItems(pageResult.getTotalElements())
                .pageSize(pageResult.getSize())
                .items(pageResult.getContent())  //  FULL Product + NEWEST FIRST
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
    }

    // 5. FULL Product open source pagination (NEWEST FIRST)
    public PaginationResponse<Product> getOpenSourceProductsPaginated(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var pageResult = productRepository.findByIsOpenSourceTrueOrderByCreatedAtDesc(pageable);

        return PaginationResponse.<Product>builder()
                .currentPage(pageResult.getNumber())
                .totalPages(pageResult.getTotalPages())
                .totalItems(pageResult.getTotalElements())
                .pageSize(pageResult.getSize())
                .items(pageResult.getContent())  //  FULL Product + NEWEST FIRST
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
    }
    // 6. DELETE - INLINE validation
    public void deleteProduct(String id, String deletedBy) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        productRepository.delete(product);
    }

    // 7. Get Product Statistics
    public ProductStatsResponse getProductStatistics() {
        MongoCollection<Document> collection = mongoTemplate.getCollection("products");

        long total = collection.countDocuments();
        long pending = collection.countDocuments(eq("status", "Pending"));
        long approved = collection.countDocuments(eq("status", "Approved"));
        long rejected = collection.countDocuments(eq("status", "Rejected"));
        long released = collection.countDocuments(eq("status", "Released"));
        long openSource = collection.countDocuments(eq("isOpenSource", true));

        log.info("Product Stats: total={}, pending={}, approved={}, rejected={}, released={}, openSource={}",
                total, pending, approved, rejected, released, openSource);

        return new ProductStatsResponse(total, pending, approved, rejected, released, openSource);
    }
}
