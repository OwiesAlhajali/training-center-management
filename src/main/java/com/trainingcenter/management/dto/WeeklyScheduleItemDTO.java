package com.trainingcenter.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
public class WeeklyScheduleItemDTO {
    private String day;
    private String courseName;
    private LocalTime startTime;
    private LocalTime endTime;
    private String teacherName;
    private String room;
}

