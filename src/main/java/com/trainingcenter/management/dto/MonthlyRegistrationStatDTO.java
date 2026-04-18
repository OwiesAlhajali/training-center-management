package com.trainingcenter.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MonthlyRegistrationStatDTO {
    private Integer month;
    private Long registrations;
}

