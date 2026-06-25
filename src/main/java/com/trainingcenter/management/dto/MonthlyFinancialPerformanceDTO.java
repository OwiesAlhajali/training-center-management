package com.trainingcenter.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MonthlyFinancialPerformanceDTO {
    private Integer month;
    private Long totalRevenue;
    private Long totalPayments;
}
