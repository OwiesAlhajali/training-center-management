package com.trainingcenter.management.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class LectureResponseDTO {
    private Long id;
    private LocalDate lectureDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String sessionName;
    private String classroomNumber;
    private String teacherName;
}
