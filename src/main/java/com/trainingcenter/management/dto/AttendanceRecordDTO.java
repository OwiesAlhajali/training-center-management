package com.trainingcenter.management.dto;

import com.trainingcenter.management.entity.AttendanceStatus;
import lombok.Data;

@Data
public class AttendanceRecordDTO {
    private Long studentId;
    private AttendanceStatus status;
}
