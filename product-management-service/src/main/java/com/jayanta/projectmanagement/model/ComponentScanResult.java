package com.jayanta.projectmanagement.model;

import lombok.Data;

@Data
public class ComponentScanResult {
    private String componentName;
    private String language;
    private Integer issuesCount;
    private Boolean isPassing;
}
