package com.trainingcenter.management.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String requirements;
    private Integer hours;
    private String categoryName;
    private String tenantName;
}