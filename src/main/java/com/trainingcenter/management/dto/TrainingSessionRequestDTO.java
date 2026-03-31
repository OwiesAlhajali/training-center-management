package com.trainingcenter.management.dto;

import com.trainingcenter.management.entity.SessionStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class TrainingSessionRequestDTO {
    private BigDecimal price;
    private Integer availableSeats;
    private Integer minSeats;
    private Integer numberOfLectures;
    private String requiredEquipment;
    private String duration;
    private SessionStatus status;
    private Long courseId;
    private Long classroomId;
    private Long teacherId;
    
    private LocalDate startDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<String> daysOfWeek; 
}
