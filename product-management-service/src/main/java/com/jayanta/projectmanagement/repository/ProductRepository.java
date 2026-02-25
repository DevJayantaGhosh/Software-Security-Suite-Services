package com.jayanta.projectmanagement.repository;

import com.jayanta.projectmanagement.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    //  1. ALL Products - Pagination + createdAt DESC (NEWEST FIRST)
    Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);

    //  2. Open Source Products - Pagination + createdAt DESC (NEWEST FIRST)
    Page<Product> findByIsOpenSourceTrueOrderByCreatedAtDesc(Pageable pageable);
}
