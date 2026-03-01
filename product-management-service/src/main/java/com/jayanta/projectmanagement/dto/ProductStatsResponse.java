package com.jayanta.projectmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStatsResponse {
    private long total;
    private long pending;
    private long approved;
    private long rejected;
    private long signed;
    private long released;
    private long openSource;
}
