package com.trainingcenter.management.dto;

import com.trainingcenter.management.entity.SessionStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class TrainingSessionResponseDTO {
    private Long id;
    private BigDecimal price;
    private Integer availableSeats;
    private Integer minSeats;
    private Integer numberOfLectures;
    private String requiredEquipment;
    private String duration;
    private SessionStatus status;
    private Long courseId;
    private String courseName;
    private String courseDescription;
    private String classroomName;
    private String teacherName;
    private Long teacherId;
    private String instituteName;
    private Long instituteId;
    private String image;
}
