package com.trainingcenter.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TeacherLectureCountDTO {
    private Long teacherId;
    private Long lectureCount;
}
