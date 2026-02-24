package com.jayanta.projectmanagement.repository;

import com.jayanta.projectmanagement.dto.ProductListDto;
import com.jayanta.projectmanagement.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    // LIGHTWEIGHT LIST PROJECTIONS (NO repos/dependencies)
    @Query(value = "{}", fields = "{id:1, name:1, version:1, isOpenSource:1, status:1, createdAt:1, updatedAt:1, repos:0, dependencies:0, description:0, productDirector:0, securityHead:0, releaseEngineers:0, remark:0}")
    Page<ProductListDto> findAllProjection(Pageable pageable);

    @Query(value = "{isOpenSource: true}", fields = "{id:1, name:1, version:1, isOpenSource:1, status:1, createdAt:1, updatedAt:1, repos:0, dependencies:0, description:0, productDirector:0, securityHead:0, releaseEngineers:0, remark:0}")
    List<ProductListDto> findOpenSourceProjection();
}
