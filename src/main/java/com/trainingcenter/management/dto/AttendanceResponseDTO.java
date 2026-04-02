package com.trainingcenter.management.dto;

import com.trainingcenter.management.entity.AttendanceStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponseDTO {
    private Long id;
    private Long studentId;
    private String studentFullName;
    private Long lectureId;
    private String lectureDate;
    private AttendanceStatus status;
    private LocalDateTime checkInTime;
    private Double attendancePercentage; 
}
