package com.trainingcenter.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ActiveCourseDTO {
    private Long id;
    private String name;
    private String description;
    private Integer hours;
    private String categoryName;
    private String tenantName;
    private Long activeSessions;
}


