package com.trainingcenter.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ActiveCourseResponseDTO {
    private Long id;
    private String title;
    private String thumbnailUrl;
    private Integer totalLessons;
    private Double remainingHours;
    private Double progressPercentage;
}
