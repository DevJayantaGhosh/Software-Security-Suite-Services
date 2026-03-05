package com.jayanta.projectmanagement.controller;

import com.jayanta.projectmanagement.dto.PaginationResponse;
import com.jayanta.projectmanagement.model.Repo;
import com.jayanta.projectmanagement.service.RepoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
@Tag(name = "Repo Management")
@SecurityRequirement(name = "bearerAuth")
public class RepoController {
    private final RepoService repoService;

    @GetMapping
    public ResponseEntity<PaginationResponse<Repo>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(repoService.getAllReposPaginated(page, size));
    }

    @GetMapping("/opensource")
    public ResponseEntity<PaginationResponse<Repo>> getOpenSource(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(repoService.getOpenSourceReposPaginated(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Repo> getOne(@PathVariable String id) {
        return ResponseEntity.ok(repoService.getRepoById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Repo> create(@Valid @RequestBody Repo repo) {
        return ResponseEntity.ok(repoService.createRepo(repo));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Repo> update(@PathVariable String id, @Valid @RequestBody Repo updateData) {
        return ResponseEntity.ok(repoService.updateRepo(id, updateData));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<String> delete(@PathVariable String id) {
        repoService.deleteRepo(id);
        return ResponseEntity.ok("Repo deleted successfully");
    }
}
