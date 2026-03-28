package com.trainingcenter.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EnrollmentResponseDTO {

    private Long id;
    private Long studentId;
    private Long trainingSessionId;
}