package com.jayanta.projectmanagement.dto;
import com.jayanta.projectmanagement.model.ProductStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductListDto {
    private String id;
    private String name;
    private String version;
    private Boolean isOpenSource;
    private String description;
    private String productDirector;
    private String securityHead;
    private List<String> releaseEngineers;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private ProductStatus status;
    private String remark;
    private String securityScanReportPath;
    private String signatureFilePath;
    private String publicKeyFilePath;
}
