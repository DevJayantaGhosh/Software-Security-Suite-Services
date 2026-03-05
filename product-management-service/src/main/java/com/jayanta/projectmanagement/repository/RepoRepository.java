package com.jayanta.projectmanagement.repository;

import com.jayanta.projectmanagement.model.Repo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RepoRepository extends MongoRepository<Repo, String> {
    Page<Repo> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Repo> findByIsOpenSourceTrueOrderByCreatedAtDesc(Pageable pageable);
    Optional<Repo> findByRepoUrl(String repoUrl);
    boolean existsByRepoUrl(String repoUrl);
}
