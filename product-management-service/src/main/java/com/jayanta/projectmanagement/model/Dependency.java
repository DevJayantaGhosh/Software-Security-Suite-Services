package com.jayanta.projectmanagement.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "dependencies")
public class Dependency {
    @Id
    private String id;

    @Field("name")
    private String name;        // e.g., "Docker", "MongoDB"

    @Field("description")
    private String description;

    @Field("createdBy")
    private String createdBy;

    @CreatedDate
    @Field("createdAt")
    private LocalDateTime createdAt;

    @Field("updatedBy")
    private String updatedBy;

    @LastModifiedDate
    @Field("updatedAt")
    private LocalDateTime updatedAt;
}
