package com.jayanta.projectmanagement.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StaticAnalysisResult extends BaseScanResult {
    private Summary summary;
    private List<ComponentScanResult> componentResults;

    @Data
    public static class Summary {
        private Integer totalIssues;
        private Integer passedChecks;
        private Integer failedChecks;
    }
}
