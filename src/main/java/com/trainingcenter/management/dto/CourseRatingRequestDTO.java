package com.trainingcenter.management.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CourseRatingRequestDTO {

    @NotNull(message = "Course ID is required ")
    private Long courseId ;

    @NotNull(message = "Rating is Required ")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
    private BigDecimal rating ;

    @Size(max = 1000, message = "Review must not exceed 1000 characters")
    private String review;
}
