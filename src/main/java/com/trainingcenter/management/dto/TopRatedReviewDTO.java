package com.trainingcenter.management.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopRatedReviewDTO {

    private Long id;

    private Long trainingSessionId;
    private String courseName;

    private Long userId;
    private String username;

    private BigDecimal rating;

    private String review;
}
