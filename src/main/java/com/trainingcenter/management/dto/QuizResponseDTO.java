package com.trainingcenter.management.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class QuizResponseDTO {

    private Long id;

    private String name;

    private BigDecimal maxScore;

    private BigDecimal passingScore;

    private LocalDateTime createdAt;

    private Long trainingSessionId;
}