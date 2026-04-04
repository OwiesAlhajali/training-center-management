package com.trainingcenter.management.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class GradeResponseDTO {

    private Long id;

    private BigDecimal score;

    private Long studentId;

    private Long quizId;
}
