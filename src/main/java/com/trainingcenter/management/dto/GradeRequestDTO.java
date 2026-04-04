package com.trainingcenter.management.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GradeRequestDTO {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.00", message = "Score must be at least 0")
    private BigDecimal score;
}
