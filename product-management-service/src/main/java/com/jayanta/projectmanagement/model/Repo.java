package com.jayanta.projectmanagement.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "repos")
public class Repo {
    @Id private String id;

    @Field("name") private String name;
    @Field("repoUrl") private String repoUrl;  // UNIQUE - No duplicates
    @Field("isOpenSource") private Boolean isOpenSource;

    @Field("createdBy") private String createdBy;
    @CreatedDate @Field("createdAt") private LocalDateTime createdAt;
    @Field("updatedBy") private String updatedBy;
    @LastModifiedDate @Field("updatedAt") private LocalDateTime updatedAt;
}
