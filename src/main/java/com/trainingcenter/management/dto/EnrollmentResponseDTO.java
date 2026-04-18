package com.trainingcenter.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EnrollmentResponseDTO {

    private Long id;
    private Long studentId;
    private Long trainingSessionId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}