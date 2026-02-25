package com.jayanta.projectmanagement.controller;

import com.jayanta.projectmanagement.dto.*;
import com.jayanta.projectmanagement.model.Product;
import com.jayanta.projectmanagement.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management")
public class ProductController {

    private final ProductService productService;

    // 1. CREATE
    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody Product createData, Authentication auth) {
        return ResponseEntity.ok(productService.createProduct(createData, auth.getName()));
    }

    // 2. UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable String id, @RequestBody Product updateData, Authentication auth) {
        return ResponseEntity.ok(productService.updateProduct(id, updateData, auth.getName()));
    }

    // 3. GET ONE
    @GetMapping("/{id}")
    public ResponseEntity<Product> getOne(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // 4. Full Product + Pagination (NEWEST FIRST)
    @GetMapping
    public ResponseEntity<PaginationResponse<Product>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getAllProductsPaginated(page, size));
    }

    // 5. Full Product + Pagination (NEWEST FIRST)
    @GetMapping("/opensource")
    public ResponseEntity<PaginationResponse<Product>> getOpenSource(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getOpenSourceProductsPaginated(page, size));
    }

    // 6. DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id, Authentication auth) {
        productService.deleteProduct(id, auth.getName());
        return ResponseEntity.ok("Product deleted successfully");
    }

    // 7. STATS
    @GetMapping("/stats")
    public ResponseEntity<ProductStatsResponse> getStatistics() {
        return ResponseEntity.ok(productService.getProductStatistics());
    }
}
