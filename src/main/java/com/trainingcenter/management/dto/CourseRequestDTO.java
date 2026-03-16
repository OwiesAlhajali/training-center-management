package com.trainingcenter.management.dto;

import lombok.Data;

@Data
public class CourseRequestDTO {
    private String name;
    private String description;
    private String requirements;
    private Integer hours;
    private Long categoryId;
    private Long tenantId;
}