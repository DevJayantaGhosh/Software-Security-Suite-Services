package com.jayanta.projectmanagement.model;

import lombok.Data;
import java.util.List;

@Data
public class BaseScanResult {
    private ScanStatus status;
    private String timestamp;
    private List<String> logs;
}
