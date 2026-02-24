package com.jayanta.projectmanagement.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "products")
public class Product {
    @Id
    private String id;

    // Basic Info
    @Field("name")
    private String name;

    @Field("version")
    private String version;  //"1.2.3-beta"

    @Field("isOpenSource")
    private Boolean isOpenSource;

    @Field("description")
    private String description;

    // Stakeholders
    @Field("productDirector")
    private String productDirector;

    @Field("securityHead")
    private String securityHead;

    @Field("releaseEngineers")
    private List<String> releaseEngineers;

    // Technical Details
    @Field("repos")
    private List<RepoDetails> repos;

    @Field("dependencies")
    private List<String> dependencies;

    // 🔥 AUTO-MANAGED AUDIT FIELDS - FIXED!
    @Field("createdBy")
    private String createdBy;

    @CreatedDate
    @Field("createdAt")
    private LocalDateTime createdAt;  // Auto-set ONCE on CREATE

    @Field("updatedBy")
    private String updatedBy;

    @LastModifiedDate
    @Field("updatedAt")
    private LocalDateTime updatedAt;  //  Auto-updated EVERY SAVE

    // Approval Workflow
    @Field("status")
    private ProductStatus status;

    @Field("remark")
    private String remark;

    @Field("signatureFilePath")
    private String signatureFilePath;

    @Field("publicKeyFilePath")
    private String publicKeyFilePath;
}
