package com.jayanta.projectmanagement.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SecretLeakDetectionResult extends BaseScanResult {
    private Summary summary;

    @Data
    public static class Summary {
        private Integer findings;
    }
}
