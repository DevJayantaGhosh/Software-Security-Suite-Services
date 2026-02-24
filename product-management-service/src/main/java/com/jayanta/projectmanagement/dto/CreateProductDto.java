package com.jayanta.projectmanagement.dto;

import com.jayanta.projectmanagement.model.RepoDetails;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreateProductDto {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Version is required")
    private String version;

    @NotNull(message = "isOpenSource is required")
    private boolean isOpenSource;


    private String description;
    private String productDirector;
    private String securityHead;
    private List<String> releaseEngineers;
    private List<RepoDetails> repos;
    private List<String> dependencies;
}


