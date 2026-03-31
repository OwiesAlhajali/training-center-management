package com.trainingcenter.management.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRatingResponseDTO {

    private Long id;

    private Long courseId;
    private String courseName;

    private Long userId;
    private String username;

    private BigDecimal rating;

    private String review;

}