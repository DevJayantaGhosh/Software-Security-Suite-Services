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

    // 🔥 FIXED: ONLY INCLUSIONS (1) - NO exclusions (0)
    @Query(value = "{}", fields = """
        {
            "_id": 1, 
            "name": 1, 
            "version": 1, 
            "isOpenSource": 1, 
            "description": 1,
            "productDirector": 1, 
            "securityHead": 1, 
            "releaseEngineers": 1,
            "createdBy": 1, 
            "createdAt": 1, 
            "updatedBy": 1, 
            "updatedAt": 1,
            "status": 1, 
            "remark": 1, 
            "signatureFilePath": 1, 
            "publicKeyFilePath": 1, 
            "securityScanReportPath": 1
        }
        """)
    Page<ProductListDto> findAllProjection(Pageable pageable);

    @Query(value = "{\"isOpenSource\": true}", fields = """
        {
            "_id": 1, 
            "name": 1, 
            "version": 1, 
            "isOpenSource": 1, 
            "description": 1,
            "productDirector": 1, 
            "securityHead": 1, 
            "releaseEngineers": 1,
            "createdBy": 1, 
            "createdAt": 1, 
            "updatedBy": 1, 
            "updatedAt": 1,
            "status": 1, 
            "remark": 1, 
            "signatureFilePath": 1, 
            "publicKeyFilePath": 1, 
            "securityScanReportPath": 1
        }
        """)
    List<ProductListDto> findOpenSourceProjection();
}
