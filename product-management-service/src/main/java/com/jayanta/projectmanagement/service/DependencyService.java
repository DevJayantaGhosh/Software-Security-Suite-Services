package com.jayanta.projectmanagement.service;

import com.jayanta.projectmanagement.dto.PaginationResponse;
import com.jayanta.projectmanagement.exception.DependencyNotFoundException;
import com.jayanta.projectmanagement.exception.DuplicateDependencyException;
import com.jayanta.projectmanagement.model.Dependency;
import com.jayanta.projectmanagement.repository.DependencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DependencyService {

    private final DependencyRepository dependencyRepository;

    public Dependency createDependency(Dependency dependency) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        if (dependencyRepository.existsByName(dependency.getName())) {
            throw new DuplicateDependencyException(
                    "Dependency name already exists: " + dependency.getName()
            );
        }

        dependency.setId(UUID.randomUUID().toString());
        dependency.setCreatedBy(user);
        dependency.setUpdatedBy(user);

        log.info("Admin '{}' creating dependency: {}", user, dependency.getName());
        return dependencyRepository.save(dependency);
    }

    public Dependency updateDependency(String id, Dependency updateData) {
        Dependency existing = dependencyRepository.findById(id)
                .orElseThrow(() -> new DependencyNotFoundException("Dependency not found: " + id));

        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        if (updateData.getName() != null && !updateData.getName().equals(existing.getName())) {
            if (dependencyRepository.existsByName(updateData.getName())) {
                throw new DuplicateDependencyException(
                        "Dependency name already exists: " + updateData.getName()
                );
            }
        }

        if (updateData.getName() != null) existing.setName(updateData.getName());
        if (updateData.getDescription() != null) existing.setDescription(updateData.getDescription());
        existing.setUpdatedBy(user);

        log.info("Admin '{}' updating dependency: {}", user, existing.getName());
        return dependencyRepository.save(existing);
    }

    public Dependency getDependencyById(String id) {
        return dependencyRepository.findById(id)
                .orElseThrow(() -> new DependencyNotFoundException("Dependency not found: " + id));
    }

    public PaginationResponse<Dependency> getAllDependenciesPaginated(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var pageResult = dependencyRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PaginationResponse.<Dependency>builder()
                .currentPage(pageResult.getNumber())
                .totalPages(pageResult.getTotalPages())
                .totalItems(pageResult.getTotalElements())
                .pageSize(pageResult.getSize())
                .items(pageResult.getContent())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
    }

    public void deleteDependency(String id) {
        if (!dependencyRepository.existsById(id)) {
            throw new DependencyNotFoundException("Dependency not found: " + id);
        }
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Admin '{}' deleting dependency: {}", user, id);
        dependencyRepository.deleteById(id);
    }
}
