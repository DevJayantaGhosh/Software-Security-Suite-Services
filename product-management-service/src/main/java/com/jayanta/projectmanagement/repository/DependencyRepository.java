package com.jayanta.projectmanagement.repository;

import com.jayanta.projectmanagement.model.Dependency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DependencyRepository extends MongoRepository<Dependency, String> {
    Page<Dependency> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Optional<Dependency> findByName(String name);
    boolean existsByName(String name);
}
