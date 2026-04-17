package com.trainingcenter.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TeacherCourseProgressDTO {
    private Long courseId;
    private String courseName;
    private Long completedSessions;
    private Long totalSessions;
    private Double progressPercentage;
}

