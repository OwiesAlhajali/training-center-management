package com.trainingcenter.management.dto;

import lombok.Data;
import java.util.List;

@Data
public class BulkAttendanceRequestDTO {
    private Long lectureId;
    private List<AttendanceRecordDTO> records;
}
