package com.trainingcenter.management.dto;

import com.trainingcenter.management.entity.SessionStatus;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TrainingSessionRequestDTO {
    private BigDecimal price;
    private Integer availableSeats;
    private Integer minSeats;
    private Integer numberOfLectures;
    private String duration;
    private SessionStatus status;
    private Long courseId;
    private Long classroomId;
    private Long teacherId;
}
