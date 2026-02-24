package com.jayanta.projectmanagement.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductListDto {
    private String id;
    private String name;
    private String version;
    private Boolean isOpenSource;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}