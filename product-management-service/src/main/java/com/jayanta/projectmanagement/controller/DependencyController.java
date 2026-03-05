package com.jayanta.projectmanagement.controller;

import com.jayanta.projectmanagement.dto.PaginationResponse;
import com.jayanta.projectmanagement.model.Dependency;
import com.jayanta.projectmanagement.service.DependencyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/dependencies")
@RequiredArgsConstructor
@Tag(name = "Dependency Management")
@SecurityRequirement(name = "bearerAuth")
public class DependencyController {

    private final DependencyService dependencyService;

    @GetMapping
    public ResponseEntity<PaginationResponse<Dependency>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(dependencyService.getAllDependenciesPaginated(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dependency> getOne(@PathVariable String id) {
        return ResponseEntity.ok(dependencyService.getDependencyById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Dependency> create(@Valid @RequestBody Dependency dependency) {
        return ResponseEntity.ok(dependencyService.createDependency(dependency));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Dependency> update(
            @PathVariable String id,
            @Valid @RequestBody Dependency updateData
    ) {
        return ResponseEntity.ok(dependencyService.updateDependency(id, updateData));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<String> delete(@PathVariable String id) {
        dependencyService.deleteDependency(id);
        return ResponseEntity.ok("Dependency deleted successfully");
    }
}
