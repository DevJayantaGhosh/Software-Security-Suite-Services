package com.jayanta.projectmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponse<T> {
    private int currentPage;           // 0, 1, 2...
    private long totalPages;           // Total available pages
    private long totalItems;           // Total records in DB
    private int pageSize;              // Items per page (10, 25, 50)
    private List<T> items;             // Current page data
    private boolean hasNext;           // Is there next page?
    private boolean hasPrevious;       // Is there previous page?
}


