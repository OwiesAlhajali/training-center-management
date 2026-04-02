package com.trainingcenter.management.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuizRequestDTO {

    @NotBlank(message = "Quiz name is required")
    @Size(max = 100, message = "Quiz name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Max score is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Max score must be greater than 0")
    @Digits(integer = 3, fraction = 2, message = "Max score must be a valid number with up to 3 digits and 2 decimals")
    private BigDecimal maxScore;

    @NotNull(message = "Passing score is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Passing score cannot be negative")
    @Digits(integer = 3, fraction = 2, message = "Passing score must be a valid number with up to 3 digits and 2 decimals")
    private BigDecimal passingScore;

    @NotNull(message = "Training session ID is required")
    private Long trainingSessionId;
}