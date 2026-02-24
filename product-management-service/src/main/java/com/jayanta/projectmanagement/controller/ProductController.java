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
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management")
public class ProductController {

    private final ProductService productService;

    //  ALL ENDPOINTS - JWT AUTHENTICATED ONLY (No role checks)
    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody CreateProductDto dto, Authentication auth) {
        return ResponseEntity.ok(productService.createProduct(dto, auth.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable String id, @RequestBody Product updateData, Authentication auth) {
        return ResponseEntity.ok(productService.updateProduct(id, updateData, auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getOne(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping
    public ResponseEntity<PaginationResponse<ProductListDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getAllProductsPaginated(page, size));
    }

    @GetMapping("/opensource")
    public ResponseEntity<List<ProductListDto>> getOpenSource() {
        return ResponseEntity.ok(productService.getOpenSourceProducts());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id, Authentication auth) {
        productService.deleteProduct(id, auth.getName());
        return ResponseEntity.ok("Product deleted successfully");
    }
}
