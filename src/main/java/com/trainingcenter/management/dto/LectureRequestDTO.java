package com.trainingcenter.management.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureRequestDTO {

    @NotNull(message = "Lecture date is required")
    @FutureOrPresent(message = "Lecture date cannot be in the past")
    private LocalDate lectureDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Classroom ID is required")
    private Long classroomId;

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    // Required when creating a new lecture, optional for updates
    private Long sessionId;
}
