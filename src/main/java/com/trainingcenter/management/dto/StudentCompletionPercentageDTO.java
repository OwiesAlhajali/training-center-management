package com.trainingcenter.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class StudentCompletionPercentageDTO {
    private Long studentId;
    private Long completedSessions;
    private Long totalSessions;
    private Double completionPercentage;
}

