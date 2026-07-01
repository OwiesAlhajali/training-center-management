package com.trainingcenter.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class StudentCompletionPercentageDTO {
    private Long studentId;
    private Long trainingSessionId;
    private String courseName;
    private Long totalLectures;
    private Long lecturesAttended;
    private String image;
    private LocalDate startDate;
    private Double attendancePercentage;
}
