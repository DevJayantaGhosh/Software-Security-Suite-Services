package com.jayanta.projectmanagement.service;

import com.jayanta.projectmanagement.dto.PaginationResponse;
import com.jayanta.projectmanagement.exception.RepoNotFoundException;
import com.jayanta.projectmanagement.exception.DuplicateRepoException;
import com.jayanta.projectmanagement.model.Repo;
import com.jayanta.projectmanagement.repository.RepoRepository;
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
public class RepoService {
    private final RepoRepository repoRepository;

    public Repo createRepo(Repo repo) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        if (repoRepository.existsByRepoUrl(repo.getRepoUrl())) {
            throw new DuplicateRepoException("Repository URL already exists: " + repo.getRepoUrl());
        }

        repo.setId(UUID.randomUUID().toString());
        repo.setCreatedBy(user);
        repo.setUpdatedBy(user);

        log.info("Admin '{}' creating repo: {}", user, repo.getName());
        return repoRepository.save(repo);
    }

    public Repo updateRepo(String id, Repo updateData) {
        Repo existing = repoRepository.findById(id)
                .orElseThrow(() -> new RepoNotFoundException("Repo not found: " + id));

        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        if (updateData.getRepoUrl() != null && !updateData.getRepoUrl().equals(existing.getRepoUrl())) {
            if (repoRepository.existsByRepoUrl(updateData.getRepoUrl())) {
                throw new DuplicateRepoException("Repository URL already exists: " + updateData.getRepoUrl());
            }
        }

        if (updateData.getName() != null) existing.setName(updateData.getName());
        if (updateData.getRepoUrl() != null) existing.setRepoUrl(updateData.getRepoUrl());
        if (updateData.getIsOpenSource() != null) existing.setIsOpenSource(updateData.getIsOpenSource());
        existing.setUpdatedBy(user);

        return repoRepository.save(existing);
    }

    public Repo getRepoById(String id) {
        return repoRepository.findById(id)
                .orElseThrow(() -> new RepoNotFoundException("Repo not found: " + id));
    }

    public PaginationResponse<Repo> getAllReposPaginated(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var pageResult = repoRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PaginationResponse.<Repo>builder()
                .currentPage(pageResult.getNumber())
                .totalPages(pageResult.getTotalPages())
                .totalItems(pageResult.getTotalElements())
                .pageSize(pageResult.getSize())
                .items(pageResult.getContent())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
    }

    public PaginationResponse<Repo> getOpenSourceReposPaginated(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var pageResult = repoRepository.findByIsOpenSourceTrueOrderByCreatedAtDesc(pageable);
        return PaginationResponse.<Repo>builder()
                .currentPage(pageResult.getNumber())
                .totalPages(pageResult.getTotalPages())
                .totalItems(pageResult.getTotalElements())
                .pageSize(pageResult.getSize())
                .items(pageResult.getContent())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
    }

    public void deleteRepo(String id) {
        if (!repoRepository.existsById(id)) {
            throw new RepoNotFoundException("Repo not found: " + id);
        }
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Admin '{}' deleting repo: {}", user, id);
        repoRepository.deleteById(id);
    }
}
